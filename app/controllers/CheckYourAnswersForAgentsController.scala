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

import cats.data.Validated.{Invalid, Valid}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction, IdentifyAgentAction}
import models.DraftId
import models.TraderDetailsWithCountryCode
import models.requests._
import pages.Page
import pages.WhatIsYourRoleAsImporterPage
import services.SubmissionService
import viewmodels.checkAnswers.summary.ApplicationSummary
import views.html.CheckYourAnswersForAgentsView

class CheckYourAnswersForAgentsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  isAgent: IdentifyAgentAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersForAgentsView,
  submissionService: SubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(this.getClass)

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen isAgent andThen getData(draftId) andThen requireData) {
      implicit request =>
        val traderDetails: Future[TraderDetailsWithCountryCode] = ???

        val applicationSummary = ApplicationSummary(request.userAnswers, request.affinityGroup, ???)

        request.userAnswers.get(WhatIsYourRoleAsImporterPage) match {
          case Some(role) => Ok(view(applicationSummary, role, draftId))
          case None       =>
            logger.warn(
              "Invalid journey: User navigated to check your answers without specifying agent role"
            )
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen isAgent andThen getData(draftId) andThen requireData).async {
      implicit request =>
        ApplicationRequest(request.userAnswers, request.affinityGroup) match {
          case Invalid(errors: cats.data.NonEmptyList[Page]) =>
            logger.error(s"Failed to create application request: ${errors.toList.mkString(", ")}}")
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          case Valid(applicationRequest)                     =>
            submissionService
              .submitApplication(applicationRequest, request.userId)
              .map {
                response =>
                  Redirect(
                    routes.ApplicationCompleteController.onPageLoad(response.applicationId.toString)
                  )
              }
        }
    }
}
