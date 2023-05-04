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

import java.time.{Clock, Instant}
import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import audit.AuditService
import connectors.BackendConnector
import controllers.actions._
import models.{ApplicationForAccountHome, CounterId, DraftId, UserAnswers}
import models.AuthUserType
import navigation.Navigator
import pages.ApplicantUserType
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
  clock: Clock,
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
                navigator.startApplicationRouting(request.affinityGroup, draft.id)
              )
          }

          val viewModels = (applicationViewModels ++ draftViewModels).sortBy(_.date).reverse
          Ok(view(viewModels))
        }
    }

  def startApplication: Action[AnyContent] =
    identify.async {
      implicit request =>
        AuthUserType(request) match {
          case None           =>
            // TODO: Update error handling
            Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          case Some(authType) =>
            for {
              nextId      <- counterRepository.nextId(CounterId.DraftId)
              draftId      = DraftId(nextId)
              userAnswers  = UserAnswers(request.userId, draftId, lastUpdated = Instant.now(clock))
              userAnswers <- userAnswers.setFuture(ApplicantUserType, authType)
              _           <- userAnswersService.set(userAnswers)
            } yield Redirect(navigator.startApplicationRouting(request.affinityGroup, draftId))
        }
    }
}
