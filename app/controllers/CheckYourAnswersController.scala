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

import cats.data.Validated.{Invalid, Valid}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import controllers.common.TraderDetailsHelper
import models.{DraftId, TraderDetailsWithCountryCode}
import models.requests._
import pages.{Page, VerifyTraderDetailsPage}
import services.SubmissionService
import userrole.UserRoleProvider
import viewmodels.checkAnswers.summary.ApplicationSummaryService

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  submissionService: SubmissionService,
  userRoleProvider: UserRoleProvider,
  applicationRequestService: ApplicationRequestService,
  applicationSummaryService: ApplicationSummaryService,
  implicit val backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  private implicit val logger: Logger = Logger(this.getClass)

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        getTraderDetails {
          traderDetails =>
            val applicationSummary =
              applicationSummaryService.getApplicationSummary(request.userAnswers, traderDetails)
            Future.successful(
              Ok(
                userRoleProvider
                  .getUserRole(request.userAnswers)
                  .selectViewForCheckYourAnswers(
                    applicationSummary,
                    draftId
                  )
              )
            )
        }
    }
  def onSubmit(draftId: DraftId): Action[AnyContent]   =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        if (userRoleProvider.getUserRole(request.userAnswers).sourceFromUA) {
          request.userAnswers.get(VerifyTraderDetailsPage) match {
            case Some(td) => redirectJourney(request, td.withoutConfirmation)
            case None     =>
              logger.error(
                "[CheckYourAnswersController][onSubmit] VerifyTraderDetailsPage needs to be answered(CheckYourAnswersController)"
              )
              throw new Exception(
                "VerifyTraderDetailsPage needs to be answered(CheckYourAnswersController)"
              )

          }

        } else {
          getTraderDetails({ traderDetails => redirectJourney(request, traderDetails) })
        }

    }

  private def redirectJourney(
    request: DataRequest[AnyContent],
    traderDetails: TraderDetailsWithCountryCode
  )(implicit
    hc: HeaderCarrier
  ) =
    applicationRequestService(
      request.userAnswers,
      traderDetails
    ) match {
      case Invalid(errors: cats.data.NonEmptyList[Page]) =>
        logger.warn(
          s"[CheckYourAnswersController][redirectJourney] Failed to create application request: ${errors.toList
              .mkString(", ")}}"
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

}
