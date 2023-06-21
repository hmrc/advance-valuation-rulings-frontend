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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import models.{DraftId, NormalMode}
import models.AuthUserType.IndividualTrader
import navigation.Navigator
import pages.{AccountHomePage, RequiredInformationPage}
import services.UserAnswersService
import views.html.{RequiredInformationView, TraderAgentRequiredInformationView}

class RequiredInformationController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  individualView: RequiredInformationView,
  agentView: TraderAgentRequiredInformationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = play.api.Logger(getClass)

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        logger.info("RequiredInformationController onPageLoad")

        AccountHomePage.get() match {
          case None =>
            Redirect(routes.UnauthorisedController.onPageLoad)

          case Some(IndividualTrader) =>
            Ok(individualView(draftId))
          case Some(_)                =>
            Ok(agentView(draftId))
        }
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        Redirect(
          navigator.nextPage(RequiredInformationPage, NormalMode, request.userAnswers)
        )
    }
}
