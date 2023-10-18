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

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import config.FrontendAppConfig
import connectors.BackendConnector
import controllers.actions._
import controllers.common.TraderDetailsHelper
import models._
import navigation.Navigator
import pages.{AccountHomePage, CheckRegisteredDetailsPage, EORIBeUpToDatePage, Page}
import services.UserAnswersService
import userrole.UserRoleProvider
import views.html.CheckRegisteredDetailsView

class CheckRegisteredDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  userRoleProvider: UserRoleProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CheckRegisteredDetailsView,
  implicit val backendConnector: BackendConnector,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  private implicit val logger: Logger = Logger(this.getClass)

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val formProvider = getFormForRole(request.userAnswers)
        val form         = CheckRegisteredDetailsPage.get match {
          case Some(value) => formProvider.fill(value)
          case None        => formProvider
        }

        getTraderDetails {
          (details: TraderDetailsWithCountryCode) =>
            if (appConfig.agentOnBehalfOfTrader) {
              Future.successful(
                Ok(
                  userRoleProvider
                    .getUserRole(request.userAnswers)
                    .selectViewForCheckRegisteredDetails(
                      form,
                      details,
                      mode,
                      draftId
                    )
                )
              )
            } else {
              AccountHomePage.get() match {
                case None               =>
                  Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
                case Some(authUserType) =>
                  Future.successful(
                    Ok(view(form, details, mode, authUserType, draftId))
                  )
              }
            }
        }
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val form = getFormForRole(request.userAnswers)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              getTraderDetails {
                (details: TraderDetailsWithCountryCode) =>
                  if (appConfig.agentOnBehalfOfTrader) {
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
                  } else {
                    AccountHomePage.get() match {
                      case None               =>
                        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
                      case Some(authUserType) =>
                        Future.successful(
                          BadRequest(
                            view(formWithErrors, details, mode, authUserType, draftId)
                          )
                        )
                    }
                  }
              },
            value =>
              for {
                updatedAnswers <- CheckRegisteredDetailsPage.set(value)
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(if (appConfig.agentOnBehalfOfTrader) {
                navigator.nextPage(getNextPage(value, updatedAnswers), mode, updatedAnswers)
              } else {
                navigator.nextPage(CheckRegisteredDetailsPage, mode, updatedAnswers)
              })
          )
    }

  private def getNextPage(value: Boolean, userAnswers: UserAnswers): Page =
    if (value) {
      userRoleProvider.getUserRole(userAnswers).selectGetRegisteredDetailsPage()
    } else {
      EORIBeUpToDatePage
    }

  private def getFormForRole(userAnswers: UserAnswers) =
    userRoleProvider.getUserRole(userAnswers).getFormForCheckRegisteredDetails
}
