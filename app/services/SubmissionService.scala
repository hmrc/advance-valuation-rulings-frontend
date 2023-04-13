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

package services

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HeaderCarrier

import connectors.BackendConnector
import logging.Logging
import models.requests.{ApplicationRequest, ApplicationSubmissionResponse}
import services.email.EmailService

class SubmissionService @Inject() (backendConnector: BackendConnector, emailService: EmailService)(
  implicit ec: ExecutionContext
) extends Logging {

  def submitApplication(
    applicationRequest: ApplicationRequest
  )(implicit hc: HeaderCarrier): Future[ApplicationSubmissionResponse] =
    backendConnector
      .submitApplication(applicationRequest)
      .flatMap {
        submissionResponse =>
          emailService
            .sendConfirmationEmail(
              applicationRequest.contact.email,
              applicationRequest.contact.name
            )
            .map(_ => submissionResponse)
            .recover {
              case err: Throwable =>
                logger.warn(
                  s"Failed to send an email for application ${submissionResponse.applicationId.toString}",
                  err
                )
                submissionResponse
            }
      }
}
