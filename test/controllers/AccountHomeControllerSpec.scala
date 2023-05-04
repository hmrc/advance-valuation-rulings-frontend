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

import java.time.{Clock, Instant, ZoneOffset}

import scala.concurrent.Future

import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup

import audit.AuditService
import base.SpecBase
import connectors.BackendConnector
import models.{ApplicationForAccountHome, Done, DraftId}
import models.UserAnswers
import models.requests._
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import services.UserAnswersService
import views.html.AccountHomeView

class AccountHomeControllerSpec extends SpecBase with MockitoSugar {

  private val mockBackEndConnector   = mock[BackendConnector]
  private val mockAuditService       = mock[AuditService]
  private val mockUserAnswersService = mock[UserAnswersService]

  override def beforeEach(): Unit = {
    reset(mockBackEndConnector, mockAuditService, mockUserAnswersService)
    super.beforeEach()
  }

  private val firstApplicationDate  = Instant.ofEpochMilli(1682525788) // April 2023
  private val secondApplicationDate = Instant.ofEpochMilli(1683130564) // May 2023

  "AccountHome Controller" - {

    "must return OK and the correct view for a GET with no applications or drafts" in {

      val response = ApplicationSummaryResponse(Nil)
      when(mockBackEndConnector.applicationSummaries(any())).thenReturn(Future.successful(response))
      when(mockUserAnswersService.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(Nil)))

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService),
          bind[UserAnswersService].to(mockUserAnswersService)
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

    "must return OK and the correct view for a GET with some applications and no drafts" in {
      val appsSummary: Seq[ApplicationSummary] =
        Seq(
          ApplicationSummary(ApplicationId(1234L), "socks", firstApplicationDate, "eoriStr"),
          ApplicationSummary(ApplicationId(1235L), "shoes", secondApplicationDate, "eoriStr")
        )

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService),
          bind[UserAnswersService].to(mockUserAnswersService)
        )
        .build()

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) thenReturn Future
        .successful(ApplicationSummaryResponse(appsSummary))

      when(mockUserAnswersService.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(Nil)))

      running(application) {
        val request = FakeRequest(GET, routes.AccountHomeController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AccountHomeView]

        val appsForAccountHome: Seq[ApplicationForAccountHome] =
          for (app <- appsSummary) yield ApplicationForAccountHome(app)(messages(application))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(appsForAccountHome.reverse)(
          request,
          messages(application)
        ).toString
      }

      verify(mockAuditService, times(1)).sendUserTypeEvent()(any(), any(), any())
    }

    "must return OK and the correct view for a GET with some drafts and no applications" in {

      val draftSummaries = Seq(DraftSummary(DraftId(0), None, Instant.now, None))

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService),
          bind[UserAnswersService].to(mockUserAnswersService)
        )
        .build()

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) thenReturn Future
        .successful(ApplicationSummaryResponse(Nil))

      when(mockUserAnswersService.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(draftSummaries)))

      running(application) {
        val request = FakeRequest(GET, routes.AccountHomeController.onPageLoad().url)

        val result = route(application, request).value

        val view      = application.injector.instanceOf[AccountHomeView]
        val navigator = application.injector.instanceOf[Navigator]

        val draftsForAccountHome = draftSummaries.map {
          d =>
            ApplicationForAccountHome(
              d,
              navigator.startApplicationRouting(AffinityGroup.Individual, d.id)
            )(messages(application))
        }

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(draftsForAccountHome)(
          request,
          messages(application)
        ).toString
      }

      verify(mockAuditService, times(1)).sendUserTypeEvent()(any(), any(), any())
    }

    "must return OK and the correct view for a GET with some drafts and some applications" in {
      val appsSummary: Seq[ApplicationSummary] =
        Seq(
          ApplicationSummary(ApplicationId(1234L), "socks", firstApplicationDate, "eoriStr"),
          ApplicationSummary(ApplicationId(1235L), "shoes", secondApplicationDate, "eoriStr")
        )

      val draftSummaries = Seq(DraftSummary(DraftId(0), None, Instant.now, None))

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService),
          bind[UserAnswersService].to(mockUserAnswersService)
        )
        .build()

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) thenReturn Future
        .successful(ApplicationSummaryResponse(appsSummary))

      when(mockUserAnswersService.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(draftSummaries)))

      running(application) {
        val request = FakeRequest(GET, routes.AccountHomeController.onPageLoad().url)

        val result = route(application, request).value

        val view      = application.injector.instanceOf[AccountHomeView]
        val navigator = application.injector.instanceOf[Navigator]

        val appsForAccountHome: Seq[ApplicationForAccountHome] =
          for (app <- appsSummary) yield ApplicationForAccountHome(app)(messages(application))

        val draftsForAccountHome = draftSummaries.map {
          d =>
            ApplicationForAccountHome(
              d,
              navigator.startApplicationRouting(AffinityGroup.Individual, d.id)
            )(messages(application))
        }

        val viewModels = (appsForAccountHome ++ draftsForAccountHome).sortBy(_.date).reverse

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModels)(
          request,
          messages(application)
        ).toString
      }

      verify(mockAuditService, times(1)).sendUserTypeEvent()(any(), any(), any())
    }

    "must REDIRECT and set ApplicantUserType on startApplication" in {
      val fixedTime = Instant.parse("2018-08-22T10:00:00Z")
      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[UserAnswersService].to(mockUserAnswersService))
          .overrides(
            bind[Clock]
              .toInstance(Clock.fixed(fixedTime, ZoneOffset.UTC))
          )
          .build()

      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      running(application) {
        val request = FakeRequest(POST, routes.AccountHomeController.startApplication().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        val expect = UserAnswers(
          userId = "id",
          draftId = DraftId(DraftIdSequence),
          data = Json.obj(
            "applicantUserType" -> "IndividualTrader"
          ),
          lastUpdated = fixedTime
        )

        verify(mockUserAnswersService, times(1)).set(eqTo(expect))(any())
      }
    }
  }
}
