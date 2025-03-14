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

package services.email

import base.SpecBase
import connectors.EmailConnector
import models.requests.{Email, EmailRequest}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockEmailConnector  = mock(classOf[EmailConnector])
  private given hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    reset(mockEmailConnector)
    super.beforeEach()
  }

  val service = new EmailService(mockEmailConnector)

  ".sendConfirmationEmail" - {

    "must send a confirmation email" in {

      when(mockEmailConnector.sendEmail(any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, "foo")))

      val name  = "name"
      val email = "test@example.com"

      val expectedEmailRequest =
        EmailRequest(to = List(Email(email)), parameters = Map("name" -> name))

      service.sendConfirmationEmail(email, name).futureValue
      verify(mockEmailConnector, times(1)).sendEmail(eqTo(expectedEmailRequest))(any())
    }

    "must fail when the connector call fails" in {

      when(mockEmailConnector.sendEmail(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("foo")))

      val name  = "name"
      val email = "test@example.com"

      service.sendConfirmationEmail(email, name).failed.futureValue
    }
  }
}
