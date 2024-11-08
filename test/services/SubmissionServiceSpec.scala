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

import base.SpecBase
import connectors.BackendConnector
import models.Done
import models.requests._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import play.api.Application
import play.api.inject.bind
import services.email.EmailService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SubmissionServiceSpec extends SpecBase {

  private val mockBackendConnector: BackendConnector     = mock(classOf[BackendConnector])
  private val mockEmailService: EmailService             = mock(classOf[EmailService])
  private val mockUserAnswersService: UserAnswersService = mock(classOf[UserAnswersService])

  private val app: Application = applicationBuilder()
    .overrides(
      bind[BackendConnector].toInstance(mockBackendConnector),
      bind[UserAnswersService].toInstance(mockUserAnswersService),
      bind[EmailService].toInstance(mockEmailService)
    )
    .build()

  private val service: SubmissionService = app.injector.instanceOf[SubmissionService]

  private val applicationRequest: ApplicationRequest = ApplicationRequest(
    draftId = draftId,
    trader = TraderDetail("eori", "name", "line1", None, None, "postcode", "GB", None, Some(false)),
    agent = None,
    contact = ContactDetails("name", "email", None, None, None),
    requestedMethod = MethodOne(None, None, None),
    goodsDetails = GoodsDetails("description", None, None, None, None, None),
    attachments = Nil,
    whatIsYourRole = WhatIsYourRole.EmployeeOrg,
    letterOfAuthority = None
  )

  private given hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    reset(mockBackendConnector)
    reset(mockEmailService)
    reset(mockUserAnswersService)
    super.beforeEach()
  }

  ".submitApplication" - {

    "must submit an application, clear user answers, send a confirmation email and return the submission response" in {

      val response = ApplicationSubmissionResponse(ApplicationId(1))

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.successful(response))
      when(mockUserAnswersService.clear(any())(any())).thenReturn(Future.successful(Done))
      when(mockEmailService.sendConfirmationEmail(any(), any())(any()))
        .thenReturn(Future.successful(Done))

      val result = service.submitApplication(applicationRequest, userAnswersId).futureValue
      result mustEqual response

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockUserAnswersService, times(1)).clear(eqTo(draftId))(any())
      verify(mockEmailService, times(1)).sendConfirmationEmail(
        eqTo(applicationRequest.contact.email),
        eqTo(applicationRequest.contact.name)
      )(any())
    }

    "must return the response when the submission succeeds but sending the confirmation email fails" in {

      val response = ApplicationSubmissionResponse(ApplicationId(1))

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.successful(response))
      when(mockUserAnswersService.clear(any())(any())).thenReturn(Future.successful(Done))
      when(mockEmailService.sendConfirmationEmail(any(), any())(any()))
        .thenReturn(Future.failed(new RuntimeException("foo")))

      val result = service.submitApplication(applicationRequest, userAnswersId).futureValue
      result mustEqual response

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockUserAnswersService, times(1)).clear(eqTo(draftId))(any())
      verify(mockEmailService, times(1)).sendConfirmationEmail(
        eqTo(applicationRequest.contact.email),
        eqTo(applicationRequest.contact.name)
      )(any())
    }

    "must return the response when the submission succeeds but clearing the user answers fail" in {

      val response = ApplicationSubmissionResponse(ApplicationId(1))

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.successful(response))
      when(mockUserAnswersService.clear(any())(any()))
        .thenReturn(Future.failed(new Exception("Failed to clear user answers")))
      when(mockEmailService.sendConfirmationEmail(any(), any())(any()))
        .thenReturn(Future.successful(Done))

      val result = service.submitApplication(applicationRequest, userAnswersId).futureValue
      result mustEqual response

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockUserAnswersService, times(1)).clear(eqTo(draftId))(any())
      verify(mockEmailService, times(1)).sendConfirmationEmail(
        eqTo(applicationRequest.contact.email),
        eqTo(applicationRequest.contact.name)
      )(any())
    }

    "must return a failed future when submitting the application fails" in {

      when(mockBackendConnector.submitApplication(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("foo")))

      service.submitApplication(applicationRequest, userAnswersId).failed.futureValue

      verify(mockBackendConnector, times(1)).submitApplication(eqTo(applicationRequest))(any())
      verify(mockEmailService, never).sendConfirmationEmail(any(), any())(any())
    }
  }
}
