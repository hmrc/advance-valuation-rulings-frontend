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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import controllers.common.TraderDetailsHelper
import forms.CheckRegisteredDetailsFormProvider
import models._
import navigation.Navigator
import pages.{AccountHomePage, CheckRegisteredDetailsPage, EORIBeUpToDatePage, Page}
import services.UserAnswersService
import userrole.UserRoleProvider

class CheckRegisteredDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: CheckRegisteredDetailsFormProvider,
  userRoleProvider: UserRoleProvider,
  val controllerComponents: MessagesControllerComponents,
  implicit val backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  private implicit val logger = Logger(this.getClass)

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        CheckRegisteredDetailsPage.get() match {
          case Some(value) =>
            getTraderDetails(
              (details: TraderDetailsWithCountryCode) =>
                AccountHomePage.get() match {
                  case None               =>
                    Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
                  case _    =>
                    Ok(
                      userRoleProvider
                        .getUserRole(request.userAnswers)
                          formProvider().fill(value),
                        .selectViewForCheckRegisteredDetails(
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
                    Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
                  case _    =>
                      userRoleProvider
                    Ok(
                        .getUserRole(request.userAnswers)
                        .selectViewForCheckRegisteredDetails(formProvider(), details, mode, draftId)
                    )
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
                      Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
                    case _    =>
                      BadRequest(
                          .getUserRole(request.userAnswers)
                        userRoleProvider
                          .selectViewForCheckRegisteredDetails(
                            formWithErrors,
                            details,
                            mode,
                            draftId
                          )
                      )
                  }
              ),
            value =>
              for {
                updatedAnswers <- CheckRegisteredDetailsPage.set(value)
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(
                  getNextPage(value, updatedAnswers),
                  mode,
                  updatedAnswers
                )
              )
          )
    }

  private def getNextPage(value: Boolean, userAnswers: UserAnswers): Page =
    if (value) {
      userRoleProvider.getUserRole(userAnswers).selectGetRegisteredDetailsPage()
    } else {
      EORIBeUpToDatePage
    }
}
