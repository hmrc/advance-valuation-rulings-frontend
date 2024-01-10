/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions._
import forms.VerifyTraderDetailsFormProvider
import models.{DraftId, Mode, TraderDetailsWithConfirmation}
import pages.{CheckRegisteredDetailsPage, VerifyTraderDetailsPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{VerifyPrivateTraderDetailView, VerifyPublicTraderDetailView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VerifyTraderEoriController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: VerifyTraderDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  publicView: VerifyPublicTraderDetailView,
  privateView: VerifyPrivateTraderDetailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def checkForm(
    details: TraderDetailsWithConfirmation,
    isChecked: Option[Boolean]
  ): Form[String] =
    isChecked match {
      case Some(isChecked) => formProvider(Some(details)).fill(isChecked.toString)
      case None            => formProvider(Some(details))
    }

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      val checked = CheckRegisteredDetailsPage.get()

      VerifyTraderDetailsPage.get() match {
        case None                                                       =>
          traderDetailsNotFoundInSession(mode, draftId)
        case Some(details) if details.consentToDisclosureOfPersonalData =>
          Ok(publicView(checkForm(details, checked), mode, draftId, details))
        case Some(details)                                              =>
          Ok(privateView(checkForm(details, checked), mode, draftId, details))
      }
    }

  private def traderDetailsNotFoundInSession(mode: Mode, draftId: DraftId) = {
    logger.warn(
      "[VerifyTraderEoriController][traderDetailsNotFoundInSession] No trader details found in session"
    )
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad(
        continueUrl = Some(
          RedirectUrl(controllers.routes.ProvideTraderEoriController.onPageLoad(mode, draftId).url)
        )
      )
    )
  }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      formProvider(VerifyTraderDetailsPage.get())
        .bindFromRequest()
        .fold(
          formWithErrors =>
            VerifyTraderDetailsPage.get() match {
              case None                                                       => Future.successful(traderDetailsNotFoundInSession(mode, draftId))
              case Some(details) if details.consentToDisclosureOfPersonalData =>
                Future.successful(BadRequest(publicView(formWithErrors, mode, draftId, details)))
              case Some(details)                                              =>
                Future.successful(BadRequest(privateView(formWithErrors, mode, draftId, details)))
            },
          value =>
            VerifyTraderDetailsPage.get() match {
              case None          => Future.successful(traderDetailsNotFoundInSession(mode, draftId))
              case Some(details) =>
                val continue = value.toBoolean
                for {
                  updatedAnswers <- {
                    VerifyTraderDetailsPage
                      .set(details.copy(confirmation = Some(continue)))
                    CheckRegisteredDetailsPage
                      .set(continue)
                  }
                  _              <- userAnswersService.set(updatedAnswers)
                } yield Redirect(
                  if (continue) {
                    routes.UploadLetterOfAuthorityController
                      .onPageLoad(mode, draftId, None, None, redirectedFromChangeButton = false)
                  } else {
                    routes.EORIBeUpToDateController.onPageLoad(draftId)
                  }
                )
            }
        )
    }
}
