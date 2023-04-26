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
import connectors.BackendConnector
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import models._
import models.DraftId
import models.requests._
import pages.Page
import services.SubmissionService
import viewmodels.checkAnswers.summary.ApplicationSummary
import views.html.CheckYourAnswersView

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  submissionService: SubmissionService,
  backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(this.getClass)

  private def getTraderDetails(
    handleSuccess: TraderDetailsWithCountryCode => Future[play.api.mvc.Result]
  )(implicit request: DataRequest[AnyContent]) =
    backendConnector
      .getTraderDetails(
        AcknowledgementReference(request.userAnswers.draftId),
        EoriNumber(request.eoriNumber)
      )
      .flatMap {
        case Right(traderDetails) =>
          handleSuccess(traderDetails)
        case Left(backendError)   =>
          logger.error(s"Failed to get trader details from backend: $backendError")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        getTraderDetails {
          traderDetails =>
            val applicationSummary =
              ApplicationSummary(request.userAnswers, request.affinityGroup, traderDetails)
            Future.successful(Ok(view(applicationSummary, draftId)))
        }
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        getTraderDetails {
          traderDetails =>
            ApplicationRequest(request.userAnswers, request.affinityGroup, traderDetails) match {
              case Invalid(errors: cats.data.NonEmptyList[Page]) =>
                logger.warn(
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

        }
    }
}
