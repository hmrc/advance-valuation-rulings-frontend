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
import scala.util.{Failure, Success}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import controllers.common.TraderDetailsHelper
import forms.TraderEoriNumberFormProvider
import models.{DraftId, EoriNumber, Mode, NormalMode, TraderDetailsWithCountryCode}
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
  implicit val backendConnector: BackendConnector,
  provideTraderEoriView: ProvideTraderEoriView,
  invalidEoriView: InvalidTraderEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  val form = formProvider()

  private implicit val logger = Logger(this.getClass)

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
              request.userAnswers.set(ProvideTraderEoriPage, value) match {
                case Success(eoriAnswers) =>
                  userAnswersService.set(eoriAnswers).flatMap {
                    _ =>
                      getTraderDetails(
                        details =>
                          eoriAnswers.set[TraderDetailsWithCountryCode](
                            VerifyTraderDetailsPage,
                            details
                          ) match {
                            case Success(traderAnswers) =>
                              userAnswersService.set(traderAnswers)
                              Future.successful(
                                Redirect(
                                  navigator.nextPage(
                                    ProvideTraderEoriPage,
                                    NormalMode,
                                    request.userAnswers
                                  )
                                )
                              )
                            case Failure(error)         =>
                              logger.warn(
                                s"Unable to store VerifyTraderDetailsPage. Error: $error"
                              )
                              Future.successful(
                                Redirect(
                                  controllers.routes.JourneyRecoveryController.onPageLoad()
                                )
                              )
                          },
                        Some(Future.successful(NotFound(invalidEoriView(mode, draftId, value)))),
                        Some(EoriNumber(value))
                      )
                  }
                case Failure(error)       =>
                  logger.warn(s"Unable to store ProvideTraderEoriPage. Error: $error")
                  Future
                    .successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
          )
    }
}
