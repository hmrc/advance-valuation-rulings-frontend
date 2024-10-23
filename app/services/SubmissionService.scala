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

package services

import connectors.BackendConnector
import logging.Logging
import models.Done
import models.requests.{ApplicationId, ApplicationRequest, ApplicationSubmissionResponse}
import services.email.EmailService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionService @Inject() (
  backendConnector: BackendConnector,
  emailService: EmailService,
  userAnswersService: UserAnswersService
)(implicit
  ec: ExecutionContext
) extends Logging {

  def submitApplication(
    applicationRequest: ApplicationRequest,
    userId: String
  )(implicit hc: HeaderCarrier): Future[ApplicationSubmissionResponse] =
    for {
      submissionResponse <- backendConnector.submitApplication(applicationRequest)
      applicationId       = submissionResponse.applicationId
      contactDetails      = applicationRequest.contact
      _                  <- userAnswersService
                              .clear(applicationRequest.draftId)
                              .recover(logError(applicationId, "Failed to clear user answers")(_))
      _                  <- emailService
                              .sendConfirmationEmail(contactDetails.email, contactDetails.name)
                              .recover(logError(applicationId, "Failed to send an email")(_))
    } yield submissionResponse

  private def logError(applicationId: ApplicationId, message: String): Throwable => Done = (err: Throwable) =>
    logger.error(s"[SubmissionService][logError] $message for application $applicationId, ${err.getMessage}")
    Done
}
