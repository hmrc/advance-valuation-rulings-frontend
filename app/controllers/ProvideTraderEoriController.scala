/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import forms.TraderEoriNumberFormProvider
import models.{AcknowledgementReference, DraftId, EoriNumber, Mode, TraderDetailsWithCountryCode}
import navigation.Navigator
import pages.{ProvideTraderEoriPage, VerifyTraderDetailsPage}
import services.UserAnswersService
import views.html.{InvalidTraderEoriView, ProvideTraderEoriView}

class ProvideTraderEoriController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: TraderEoriNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  backendConnector: BackendConnector,
  provideTraderEoriView: ProvideTraderEoriView,
  invalidEoriView: InvalidTraderEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.get(ProvideTraderEoriPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(provideTraderEoriView(preparedForm, mode, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(provideTraderEoriView(formWithErrors, mode, draftId))),
            value =>
              (for {
                answersWithSearchedEori  <-
                  Future.fromTry(request.userAnswers.set(ProvideTraderEoriPage, value))
                _                        <- userAnswersService.set(answersWithSearchedEori)
                traderDetails            <- backendConnector
                                              .getTraderDetails(
                                                AcknowledgementReference(request.userAnswers.draftId),
                                                EoriNumber(value)
                                              )
                                              .map {
                                                case Right(details)                         => details
                                                case Left(error) if error.code == NOT_FOUND =>
                                                  throw new NotFoundException(
                                                    s"Provided EORI: $value is not found"
                                                  )
                                                case ex @ _                                 =>
                                                  logger.warn(s"Backend error: $ex")
                                                  throw new IllegalStateException(
                                                    s"Backend connector error"
                                                  )
                                              }
                answersWithTraderDetails <-
                  Future.fromTry(
                    request.userAnswers
                      .set[TraderDetailsWithCountryCode](VerifyTraderDetailsPage, traderDetails)
                  )
                _                        <- userAnswersService.set(answersWithTraderDetails)
              } yield Redirect(controllers.routes.VerifyTraderEoriController.onPageLoad(draftId)))
                .recover {
                  case ex: NotFoundException =>
                    logger.warn(s"Trader details not found. Error: $ex")
                    NotFound(invalidEoriView(mode, draftId, value))
                  case ex @ _                =>
                    logger.warn(s"Unable to retrieve trader details. Error: $ex")
                    Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                }
          )
    }
}
