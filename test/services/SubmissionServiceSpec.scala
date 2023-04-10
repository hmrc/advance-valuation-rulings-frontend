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

import scala.concurrent.Future

import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier

import connectors.BackendConnector
import models.Done
import models.requests._
import org.mockito.{Mockito, MockitoSugar}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import services.email.EmailService

class SubmissionServiceSpec
    extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  private val mockBackendConnector = mock[BackendConnector]
  private val mockEmailService     = mock[EmailService]

  private val app =
    GuiceApplicationBuilder()
      .overrides(
        bind[BackendConnector].toInstance(mockBackendConnector),
        bind[EmailService].toInstance(mockEmailService)
      )
      .build()

  private val service = app.injector.instanceOf[SubmissionService]

  private val applicationId      = ApplicationId(1)
  private val response           = ApplicationSubmissionResponse(applicationId)
  private val applicationRequest = ApplicationRequest(
    draftId = "draft",
    trader = TraderDetail("eori", "name", "line1", None, None, "postcode", "GB", None),
    agent = None,
    contact = ContactDetails("name", "email", None),
    requestedMethod = MethodOne(None, None, None),
    goodsDetails = GoodsDetails("name", "description", None, None, None),
    attachments = Nil
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    Mockito.reset(mockBackendConnector)
    Mockito.reset(mockEmailService)
    super.beforeEach()
  }

  ".submitApplication" - {

    "must submit an application, send a confirmation email and return the submission response" in {

      val response = ApplicationSubmissionResponse(ApplicationId(1))

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.successful(response))
      when(mockEmailService.sendConfirmationEmail(any(), any())(any()))
        .thenReturn(Future.successful(Done))

      val result = service.submitApplication(applicationRequest).futureValue
      result mustEqual response

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockEmailService, times(1)).sendConfirmationEmail(
        eqTo(applicationRequest.contact.email),
        eqTo(applicationRequest.contact.name)
      )(any())
    }

    "must return the response when the submission succeeds but sending the confirmation email fails" in {

      val response = ApplicationSubmissionResponse(ApplicationId(1))

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.successful(response))
      when(mockEmailService.sendConfirmationEmail(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("foo")))

      val result = service.submitApplication(applicationRequest).futureValue
      result mustEqual response

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockEmailService, times(1)).sendConfirmationEmail(
        eqTo(applicationRequest.contact.email),
        eqTo(applicationRequest.contact.name)
      )(any())
    }

    "must return a failed future when submitting the application fails" in {

      val response = ApplicationSubmissionResponse(ApplicationId(1))

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("foo")))

      service.submitApplication(applicationRequest).failed.futureValue

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockEmailService, never).sendConfirmationEmail(any(), any())(any())
    }
  }
}
