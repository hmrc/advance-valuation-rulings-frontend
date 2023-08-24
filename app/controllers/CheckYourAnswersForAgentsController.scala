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
import config.FrontendAppConfig
import connectors.BackendConnector
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction, IdentifyAgentAction}
import controllers.common.TraderDetailsHelper
import controllers.routes.WhatIsYourRoleAsImporterController
import models._
import models.AuthUserType._
import models.WhatIsYourRoleAsImporter.EmployeeOfOrg
import models.requests._
import pages.{AccountHomePage, Page, WhatIsYourRoleAsImporterPage}
import services.SubmissionService
import userrole.UserRoleProvider
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
  submissionService: SubmissionService,
  appConfig: FrontendAppConfig,
  userRoleProvider: UserRoleProvider,
  implicit val backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  private implicit val logger = Logger(this.getClass)

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        getTraderDetails {
          traderDetails =>
            val applicationSummary =
              ApplicationSummary(request.userAnswers, traderDetails, appConfig, userRoleProvider)
            AccountHomePage.get() match {
              case Some(OrganisationAdmin)                   =>
                Future.successful(Ok(view(applicationSummary, EmployeeOfOrg, draftId)))
              case Some(OrganisationAssistant) | Some(Agent) =>
                request.userAnswers.get(WhatIsYourRoleAsImporterPage) match {
                  case Some(importerRole) =>
                    Future.successful(Ok(view(applicationSummary, importerRole, draftId)))
                  case None               =>
                    Future.successful(
                      Redirect(
                        WhatIsYourRoleAsImporterController.onPageLoad(
                          CheckMode,
                          request.userAnswers.draftId
                        )
                      )
                    )
                }
              case unauthorised                              =>
                logger.warn(
                  s"Invalid journey: User navigated to check your answers for agents with ${unauthorised
                      .getOrElse("empty")} user type"
                )
                Future.successful(Redirect(routes.UnauthorisedController.onPageLoad))
            }
        }
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen isAgent andThen getData(draftId) andThen requireData).async {
      implicit request =>
        getTraderDetails(
          traderDetails =>
            ApplicationRequest(
              request.userAnswers,
              traderDetails,
              appConfig,
              userRoleProvider
            ) match {
              case Invalid(errors: cats.data.NonEmptyList[Page]) =>
                logger.error(
                  s"Failed to create application request: ${errors.toList.mkString(", ")}}"
                )
                Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
              case Valid(applicationRequest)                     =>
                submissionService
                  .submitApplication(applicationRequest, request.userId)
                  .map {
                    response =>
                      Redirect(
                        routes.ApplicationCompleteController
                          .onPageLoad(response.applicationId.toString)
                      )
                  }
            }
        )

    }
}
