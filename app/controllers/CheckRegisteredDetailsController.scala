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

import scala.concurrent.ExecutionContext

import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import forms.CheckRegisteredDetailsFormProvider
import models._
import models.requests.DataRequest
import navigation.Navigator
import pages.{AccountHomePage, CheckRegisteredDetailsPage}
import services.UserAnswersService
import views.html.{AgentOrgCheckRegisteredDetailsView, CheckRegisteredDetailsView, EmployeeCheckRegisteredDetailsView, TraderCheckRegisteredDetailsView}

class CheckRegisteredDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  userRole: UserRole,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: CheckRegisteredDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  employeeView: EmployeeCheckRegisteredDetailsView,
  agentOrgView: AgentOrgCheckRegisteredDetailsView,
  agentTraderView: TraderCheckRegisteredDetailsView,
  backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(this.getClass)

  private def getTraderDetails(
    handleSuccess: TraderDetailsWithCountryCode => Result
  )(implicit request: DataRequest[AnyContent]) =
    backendConnector
      .getTraderDetails(
        AcknowledgementReference(request.userAnswers.draftId),
        EoriNumber(request.eoriNumber)
      )
      .map {
        case Right(traderDetails) =>
          handleSuccess(traderDetails)
        case Left(backendError)   =>
          logger.error(s"Failed to get trader details from backend: $backendError")
          Redirect(routes.JourneyRecoveryController.onPageLoad())
      }

  private def selectView(
    form: Form[Boolean],
    userAnswers: UserAnswers,
    details: TraderDetailsWithCountryCode,
    mode: Mode,
    draftId: DraftId
  )(implicit request: DataRequest[AnyContent]): HtmlFormat.Appendable =
    userRole(userAnswers) match {
      case userRole.Employee       =>
        employeeView(
          form,
          details,
          mode,
          draftId
        )
      case userRole.AgentForOrg    =>
        agentOrgView(
          form,
          details,
          mode,
          draftId
        )
      case userRole.AgentForTrader =>
        agentTraderView(
          form,
          details,
          mode,
          draftId
        )
    }

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        CheckRegisteredDetailsPage.get() match {
          case Some(value) =>
            getTraderDetails(
              (details: TraderDetailsWithCountryCode) =>
                AccountHomePage.get() match {
                  case None               =>
                    Redirect(routes.UnauthorisedController.onPageLoad)
                  case Some(authUserType) =>
                    Ok(
                      selectView(
                        formProvider().fill(value),
                        request.userAnswers,
                        details,
                        mode,
                        draftId
                      )
                    )
                }
            )

          case None =>
            getTraderDetails(
              (details: TraderDetailsWithCountryCode) =>
                AccountHomePage.get() match {
                  case None               =>
                    Redirect(routes.UnauthorisedController.onPageLoad)
                  case Some(authUserType) =>
                    Ok(selectView(formProvider(), request.userAnswers, details, mode, draftId))
                }
            )
        }
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val form: Form[Boolean] = formProvider()

        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              getTraderDetails(
                (details: TraderDetailsWithCountryCode) =>
                  AccountHomePage.get() match {
                    case None               =>
                      Redirect(routes.UnauthorisedController.onPageLoad)
                    case Some(authUserType) =>
                      BadRequest(
                        selectView(formProvider(), request.userAnswers, details, mode, draftId)
                      )
                  }
              ),
            value =>
              for {
                updatedAnswers <- CheckRegisteredDetailsPage.set(value)
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(CheckRegisteredDetailsPage, mode, updatedAnswers)
              )
          )
    }
}
