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

import audit.AuditService
import connectors.BackendConnector
import controllers.actions._
import controllers.routes.UnauthorisedController
import models.requests.DraftSummary
import models.{ApplicationForAccountHome, AuthUserType, CounterId, DraftId, NormalMode, UserAnswers}
import navigation.Navigator
import pages.AccountHomePage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CounterRepository
import services.UserAnswersService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AccountHomeView

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
    identify.async { implicit request =>
      auditService.sendUserTypeEvent()

      AuthUserType(request) match {
        case None           =>
          Future.successful(Redirect(UnauthorisedController.onPageLoad))
        case Some(authType) =>
          for {
            applications    <- backendConnector.applicationSummaries.map(_.summaries)
            drafts          <- userAnswersService.summaries().map(_.summaries)
            draftViewModels <-
              Future.sequence(createDraftViewModels(request.userId, authType, drafts))
          } yield {
            val applicationViewModels = applications.map(ApplicationForAccountHome(_))
            val viewModels            = (applicationViewModels ++ draftViewModels).sortBy(_.date).reverse
            Ok(view(viewModels))
          }
      }
    }

  def startApplication: Action[AnyContent] =
    identify.async { implicit request =>
      AuthUserType(request) match {
        case None           =>
          Future.successful(Redirect(UnauthorisedController.onPageLoad))
        case Some(authType) =>
          for {
            nextId      <- counterRepository.nextId(CounterId.DraftId)
            draftId      = DraftId(nextId)
            userAnswers <- UserAnswers(request.userId, draftId, lastUpdated = Instant.now(clock))
                             .setFuture(AccountHomePage, authType)
            _           <- userAnswersService.set(userAnswers)
          } yield Redirect(navigator.nextPage(AccountHomePage, NormalMode, userAnswers))
      }
    }

  private def createDraftViewModels(
    userId: String,
    authType: AuthUserType,
    drafts: Seq[DraftSummary]
  )(implicit messages: Messages): Seq[Future[ApplicationForAccountHome]] =
    drafts.map { draft =>
      UserAnswers(userId, draft.id)
        .setFuture(AccountHomePage, authType)
        .map(userAnswers =>
          ApplicationForAccountHome(
            draft,
            navigator.nextPage(AccountHomePage, NormalMode, userAnswers)
          )
        )
    }
}
