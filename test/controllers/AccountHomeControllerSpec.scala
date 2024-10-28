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

import audit.AuditService
import base.SpecBase
import connectors.BackendConnector
import models.AuthUserType.IndividualTrader
import models.requests._
import models.{ApplicationForAccountHome, Done, DraftId, NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, reset, times, verify, when}
import pages.AccountHomePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.AccountHomeView

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class AccountHomeControllerSpec extends SpecBase {

  private val mockBackEndConnector   = mock(classOf[BackendConnector])
  private val mockAuditService       = mock(classOf[AuditService])
  private val mockUserAnswersService = mock(classOf[UserAnswersService])

  override def beforeEach(): Unit = {
    reset(mockBackEndConnector)
    reset(mockAuditService)
    reset(mockUserAnswersService)
    super.beforeEach()
  }

  private val april2023             = 1682525788
  private val may2023               = 1683130564
  private val firstApplicationDate  = Instant.ofEpochMilli(april2023)
  private val secondApplicationDate = Instant.ofEpochMilli(may2023)

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
      ) `thenReturn` Future
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

      val draftSummaries = Seq(DraftSummary(draftId, None, Instant.now, None))

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService),
          bind[UserAnswersService].to(mockUserAnswersService)
        )
        .build()

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) `thenReturn` Future
        .successful(ApplicationSummaryResponse(Nil))

      when(mockUserAnswersService.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(draftSummaries)))

      running(application) {
        val request = FakeRequest(GET, routes.AccountHomeController.onPageLoad().url)

        val result = route(application, request).value

        val view      = application.injector.instanceOf[AccountHomeView]
        val navigator = application.injector.instanceOf[Navigator]

        val draftsForAccountHome = draftSummaries.map { draftSummary =>
          val userAnswers =
            userAnswersAsIndividualTrader.setFuture(AccountHomePage, IndividualTrader).futureValue
          ApplicationForAccountHome(
            draftSummary,
            navigator.nextPage(AccountHomePage, NormalMode, userAnswers)
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

      val draftSummaries = Seq(DraftSummary(draftId, None, Instant.now, None))

      val application = applicationBuilder()
        .overrides(
          bind[BackendConnector].toInstance(mockBackEndConnector),
          bind[AuditService].to(mockAuditService),
          bind[UserAnswersService].to(mockUserAnswersService)
        )
        .build()

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) `thenReturn` Future
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

        val draftsForAccountHome = draftSummaries.map { draftSummary =>
          val userAnswers =
            userAnswersAsIndividualTrader.setFuture(AccountHomePage, IndividualTrader).futureValue
          ApplicationForAccountHome(
            draftSummary,
            navigator.nextPage(AccountHomePage, NormalMode, userAnswers)
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

    "must display 'no description' for applications with no goods description" in {

      val filledDescription        = "socks"
      val unfilledDescription      = ""
      val noDescriptionPlaceholder = "no description"

      val appsSummary: Seq[ApplicationSummary] =
        Seq(
          ApplicationSummary(
            ApplicationId(1234L),
            filledDescription,
            firstApplicationDate,
            "eoriStr"
          ),
          ApplicationSummary(
            ApplicationId(1235L),
            unfilledDescription,
            secondApplicationDate,
            "eoriStr"
          )
        )

      val draftSummaries = Seq(DraftSummary(draftId, None, Instant.now, None))

      when(
        mockBackEndConnector.applicationSummaries(any())
      ) `thenReturn` Future
        .successful(ApplicationSummaryResponse(appsSummary))

      when(mockUserAnswersService.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(draftSummaries)))

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

        val view      = application.injector.instanceOf[AccountHomeView]
        val navigator = application.injector.instanceOf[Navigator]

        val appsForAccountHome: Seq[ApplicationForAccountHome] =
          for (app <- appsSummary) yield ApplicationForAccountHome(app)(messages(application))

        val draftsForAccountHome = draftSummaries.map { draftSummary =>
          val userAnswers =
            userAnswersAsIndividualTrader.setFuture(AccountHomePage, IndividualTrader).futureValue
          ApplicationForAccountHome(
            draftSummary,
            navigator.nextPage(AccountHomePage, NormalMode, userAnswers)
          )(messages(application))
        }

        val viewModels = (appsForAccountHome ++ draftsForAccountHome).sortBy(_.date).reverse

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModels)(
          request,
          messages(application)
        ).toString
        contentAsString(result) must include(noDescriptionPlaceholder)
        contentAsString(result) must include(filledDescription)
      }
    }

    "must REDIRECT and set ApplicantUserType on startApplication" in {
      val fixedTime   = Instant.parse("2018-08-22T10:00:00Z")
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
