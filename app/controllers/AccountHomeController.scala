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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import audit.AuditService
import connectors.BackendConnector
import controllers.actions._
import models.{ApplicationForAccountHome, CounterId, DraftId, UserAnswers}
import navigation.Navigator
import repositories.{CounterRepository, SessionRepository}
import views.html.AccountHomeView

class AccountHomeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  counterRepository: CounterRepository,
  identify: IdentifierAction,
  backendConnector: BackendConnector,
  auditService: AuditService,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  view: AccountHomeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  def onPageLoad: Action[AnyContent] =
    identify.async {
      implicit request =>
        auditService.sendUserTypeEvent()
        backendConnector.applicationSummaries
          .map(response => Ok(view(response.summaries.map(ApplicationForAccountHome(_)))))
    }

  def startApplication: Action[AnyContent] =
    identify.async {
      implicit request =>
        for {
          draftId <- counterRepository.nextId(CounterId.DraftId)
          _       <- sessionRepository.set(UserAnswers(request.userId, DraftId(draftId)))
        } yield Redirect(navigator.startApplicationRouting(request.affinityGroup, DraftId(draftId)))
    }
}
