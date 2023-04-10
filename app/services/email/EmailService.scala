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

package services.email

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.http.HeaderCarrier

import connectors.EmailConnector
import models.Done
import models.requests.{Email, EmailRequest}

class EmailService @Inject()(emailConnector: EmailConnector)(implicit ec: ExecutionContext) {

  def makeEmailRequest(email: String, name: String): EmailRequest =
    EmailRequest(to = List(Email(email)), parameters = Map("name" -> name))

  def sendConfirmationEmail(emailAddress: String, name: String)(implicit
    hc: HeaderCarrier
  ): Future[Done] = {
    val emailRequest = makeEmailRequest(emailAddress, name)

    emailConnector
      .sendEmail(emailRequest)
      .map(_ => Done)
  }
}
