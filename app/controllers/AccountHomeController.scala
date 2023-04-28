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
import repositories.CounterRepository
import services.UserAnswersService
import views.html.AccountHomeView

class AccountHomeController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
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

        for {
          applications <- backendConnector.applicationSummaries.map(_.summaries)
          drafts       <- userAnswersService.summaries().map(_.summaries)
        } yield {
          val applicationViewModels = applications.map(ApplicationForAccountHome(_))
          val draftViewModels       = drafts.map {
            draft =>
              ApplicationForAccountHome(
                draft,
                navigator.startApplicationRouting(
                  draft.id,
                  request.affinityGroup,
                  request.credentialRole
                )
              )
          }

          val viewModels = (applicationViewModels ++ draftViewModels).sortBy(_.date).reverse
          Ok(view(viewModels))
        }
    }

  def startApplication: Action[AnyContent] =
    identify.async {
      implicit request =>
        for {
          draftId <- counterRepository.nextId(CounterId.DraftId)
          _       <- userAnswersService.set(UserAnswers(request.userId, DraftId(draftId)))
        } yield Redirect(
          navigator.startApplicationRouting(
            DraftId(draftId),
            request.affinityGroup,
            request.credentialRole
          )
        )
    }
}
