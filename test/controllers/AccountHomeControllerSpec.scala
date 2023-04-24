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

import java.time.Instant
import java.time.temporal.ChronoUnit

import scala.concurrent.Future

import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import audit.AuditService
import base.SpecBase
import connectors.BackendConnector
import models.ApplicationForAccountHome
import models.requests.{ApplicationId, ApplicationSummary, ApplicationSummaryResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import views.html.AccountHomeView

class AccountHomeControllerSpec extends SpecBase with MockitoSugar {

  private val mockBackEndConnector = mock[BackendConnector]
  private val mockAuditService     = mock[AuditService]

  override def beforeEach(): Unit = {
    reset(mockBackEndConnector, mockAuditService)
    super.beforeEach()
  }

  "AccountHome Controller" - {

    "must return OK and the correct view for a GET with no applications" in {

      val response = ApplicationSummaryResponse(Nil)
      when(mockBackEndConnector.applicationSummaries(any())).thenReturn(Future.successful(response))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.AccountHomeController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountHomeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Seq.empty)(request, messages(application)).toString
      }

      verify(mockAuditService, times(1)).sendUserTypeEvent()(any(), any(), any())
    }

    "must return OK and the correct view for a GET with some applications" in {
      val appsSummary: Seq[ApplicationSummary] =
        Seq(
          ApplicationSummary(
            ApplicationId(1234L),
            "socks",
            Instant.now.minus(2, ChronoUnit.DAYS),
            "eoriStr"
          ),
          ApplicationSummary(ApplicationId(1235L), "shoes", Instant.now, "eoriStr")
        )

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService)
        )
        .build()

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) thenReturn Future
        .successful(ApplicationSummaryResponse(appsSummary))

      running(application) {
        val request = FakeRequest(GET, routes.AccountHomeController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountHomeView]

        val sortedAppsSummary  = appsSummary.reverse
        val appsForAccountHome = sortedAppsSummary.map(ApplicationForAccountHome(_))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(appsForAccountHome)(
          request,
          messages(application)
        ).toString
      }

      verify(mockAuditService, times(1)).sendUserTypeEvent()(any(), any(), any())
      verify(mockBackEndConnector, times(1)).applicationSummaries(any())
    }

    "must REDIRECT on startApplication" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.AccountHomeController.startApplication().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
