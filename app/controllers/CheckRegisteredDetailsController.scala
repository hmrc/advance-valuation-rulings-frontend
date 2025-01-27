/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.BackendConnector
import controllers.actions._
import controllers.common.TraderDetailsHelper
import models._
import navigation.Navigator
import pages.{CheckRegisteredDetailsPage, EORIBeUpToDatePage, Page}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import userrole.UserRoleProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRegisteredDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  userRoleProvider: UserRoleProvider,
  val controllerComponents: MessagesControllerComponents,
  implicit val backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  private given logger: Logger = Logger(this.getClass)

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      val form          = getFormForRole(request.userAnswers)
      val processedForm = CheckRegisteredDetailsPage.get() match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      getTraderDetails { (details: TraderDetailsWithCountryCode) =>
        Future.successful(
          Ok(
            userRoleProvider
              .getUserRole(request.userAnswers)
              .selectViewForCheckRegisteredDetails(
                processedForm,
                details,
                mode,
                draftId
              )
          )
        )
      }
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      val form = getFormForRole(request.userAnswers)
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            getTraderDetails { (details: TraderDetailsWithCountryCode) =>
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
