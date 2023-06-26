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

import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import audit.AuditService
import connectors.BackendConnector
import controllers.actions._
import controllers.routes.UnauthorisedController
import models.{ApplicationForAccountHome, AuthUserType, CounterId, DraftId, NormalMode, UserAnswers}
import models.requests.DraftSummary
import navigation.Navigator
import pages.AccountHomePage
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
    identify.async {
      implicit request =>
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
  )(implicit messages: Messages, hc: HeaderCarrier): Seq[Future[ApplicationForAccountHome]] =
    drafts.map {
      draft =>
        UserAnswers(userId, draft.id)
          .setFuture(AccountHomePage, authType)
          .flatMap {
            userAnswers =>
              // val lastVisited: Option[pages.QuestionPage[_]] =
              //   userAnswers.get(queries.LastQuestionAnswered)
              userAnswersService
                .get(userAnswers.draftId)
                .map {
                  userAns =>
                    val lastViewed = userAns.flatMap(_.get(queries.LastQuestionAnswered))
                    lastViewed match {
                      case Some(page) =>
                        println("lastVisited: " + lastViewed)

                        ApplicationForAccountHome(
                          draft,
                          navigator.nextPage(page, NormalMode, userAnswers)
                        )
                      case None       =>
                        println("No last visited")
                        ApplicationForAccountHome(
                          draft,
                          navigator.nextPage(AccountHomePage, NormalMode, userAnswers)
                        )
                    }
                }
          }
    }
}
