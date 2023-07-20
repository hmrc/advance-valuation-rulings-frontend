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
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.VerifyTraderDetailsFormProvider
import handlers.ErrorHandler
import models.{DraftId, Mode}
import models.requests.DataRequest
import navigation.Navigator
import pages.{ProvideTraderEoriPage, VerifyTraderDetailsPage}
import services.UserAnswersService
import views.html.{VerifyPrivateTraderDetailView, VerifyPublicTraderDetailView}

class VerifyTraderEoriController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: VerifyTraderDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  error: ErrorHandler,
  publicView: VerifyPublicTraderDetailView,
  privateView: VerifyPrivateTraderDetailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        VerifyTraderDetailsPage.get() match {
          case None                                                       =>
            traderDetailsNotFoundInSession(draftId)
          case Some(details) if details.consentToDisclosureOfPersonalData =>
            Ok(publicView(form, mode, draftId, details))
          case Some(details)                                              =>
            Ok(privateView(form, mode, draftId, details))
        }
    }

  private def traderDetailsNotFoundInSession(draftId: DraftId) = {
    logger.warn("No trader details found in session")
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad(
        continueUrl =
          Some(RedirectUrl(controllers.routes.ProvideTraderEoriController.onPageLoad(draftId).url))
      )
    )
  }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              VerifyTraderDetailsPage.get() match {
                case None                                                       => Future.successful(traderDetailsNotFoundInSession(draftId))
                case Some(details) if details.consentToDisclosureOfPersonalData =>
                  Future.successful(BadRequest(publicView(formWithErrors, mode, draftId, details)))
                case Some(details)                                              =>
                  Future.successful(BadRequest(privateView(formWithErrors, mode, draftId, details)))
              },
            value =>
              for {
                updatedAnswers <- ProvideTraderEoriPage.set(value)
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(???)
          )
    }
}
