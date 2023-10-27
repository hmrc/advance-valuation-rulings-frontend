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

import play.api.{Logger, Logging}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import controllers.common.TraderDetailsHelper
import forms.VerifyTraderDetailsFormProvider
import models.{DraftId, Mode, TraderDetailsWithCountryCode, UserAnswers}
import navigation.Navigator
import pages.{CheckRegisteredDetailsPage, EORIBeUpToDatePage, Page, VerifyTraderDetailsPage}
import services.UserAnswersService
import userrole.UserRoleProvider
import views.html.{VerifyPrivateTraderDetailView, VerifyPublicTraderDetailView}

class VerifyTraderEoriController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  privateFormProvider: VerifyTraderDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  publicView: VerifyPublicTraderDetailView,
  privateView: VerifyPrivateTraderDetailView,
  userRoleProvider: UserRoleProvider,
  navigator: Navigator,
  implicit val backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with TraderDetailsHelper {

  override implicit val logger: Logger              = Logger(this.getClass)
  private def checkForm(isChecked: Option[Boolean]) =
    isChecked match {
      case Some(isChecked) => privateFormProvider().fill(isChecked.toString)
      case None            => privateFormProvider()
    }

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        val checked = CheckRegisteredDetailsPage.get()

        VerifyTraderDetailsPage.get() match {
          case None                                                       =>
            traderDetailsNotFoundInSession(mode, draftId)
          case Some(details) if details.consentToDisclosureOfPersonalData =>
            Ok(publicView(checkForm(checked), mode, draftId, details))
          case Some(details)                                              =>
            Ok(privateView(checkForm(checked), mode, draftId, details))
        }
    }

  private def traderDetailsNotFoundInSession(mode: Mode, draftId: DraftId) = {
    logger.warn("No trader details found in session")
    Redirect(
      controllers.routes.JourneyRecoveryController.onPageLoad(
        continueUrl = Some(
          RedirectUrl(controllers.routes.ProvideTraderEoriController.onPageLoad(mode, draftId).url)
        )
      )
    )
  }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        VerifyTraderDetailsPage.get() match {
          case Some(details) if details.consentToDisclosureOfPersonalData =>
            val form =
              userRoleProvider.getUserRole(request.userAnswers).getFormForCheckRegisteredDetails
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  getTraderDetails {
                    (details: TraderDetailsWithCountryCode) =>
                      Future.successful(
                        BadRequest(
                          userRoleProvider
                            .getUserRole(request.userAnswers)
                            .selectViewForCheckRegisteredDetails(
                              formWithErrors,
                              details,
                              mode,
                              draftId
                            )
                        )
                      )
                  },
                value =>
                  for {
                    updatedAnswers <- CheckRegisteredDetailsPage.set(value)
                    _              <- userAnswersService.set(updatedAnswers)
                  } yield Redirect(
                    navigator.nextPage(getNextPage(value, updatedAnswers), mode, updatedAnswers)
                  )
              )

          case Some(details) =>
            privateFormProvider()
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future
                    .successful(BadRequest(privateView(formWithErrors, mode, draftId, details))),
                value => {
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
          case None          => Future.successful(traderDetailsNotFoundInSession(mode, draftId))
        }
    }

  private def getNextPage(value: Boolean, userAnswers: UserAnswers): Page =
    if (value) {
      userRoleProvider.getUserRole(userAnswers).selectGetRegisteredDetailsPage()
    } else {
      EORIBeUpToDatePage
    }
}
