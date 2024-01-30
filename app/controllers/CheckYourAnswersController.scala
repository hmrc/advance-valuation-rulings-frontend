/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.BackendConnector
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import controllers.common.TraderDetailsHelper
import models.requests._
import models.{DraftId, TraderDetailsWithCountryCode}
import pages.{Page, VerifyTraderDetailsPage}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SubmissionService
import services.checkAnswers.ApplicationSummaryService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import userrole.UserRoleProvider

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

  private def renderPageWhenApplicationIsCompleted(traderDetails: TraderDetailsWithCountryCode, draftId: DraftId)(
    implicit
    request: DataRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    applicationRequestService(request.userAnswers, traderDetails) match {
      case Invalid(errors: cats.data.NonEmptyList[Page]) =>
        logger.warn(
          s"[CheckYourAnswersController][redirectJourney] Failed to create application request: " +
            s"${errors.toList.mkString(", ")}"
        )
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      case Valid(applicationRequest)                     =>
        val applicationSummary = applicationSummaryService.getApplicationSummary(request.userAnswers, traderDetails)
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

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      getTraderDetails { traderDetails =>
        renderPageWhenApplicationIsCompleted(traderDetails, draftId)
      }
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
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
        getTraderDetails(traderDetails => redirectJourney(request, traderDetails))
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
          .map { response =>
            Redirect(
              routes.ApplicationCompleteController
                .onPageLoad(response.applicationId.toString)
            )
          }
    }

}
