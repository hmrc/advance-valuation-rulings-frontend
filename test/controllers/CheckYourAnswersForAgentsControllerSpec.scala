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

import scala.concurrent.Future

import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import base.SpecBase
import connectors.BackendConnector
import models.{BackendError, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import viewmodels.checkAnswers.summary.ApplicationSummary
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersForAgentsView

class CheckYourAnswersForAgentsControllerSpec extends SpecBase with SummaryListFluency {

  implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val userAnswers = emptyUserAnswers.copy(affinityGroup = AffinityGroup.Organisation)

  "Check Your Answers for Agents Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilderAsAgent(userAnswers = Option(userAnswers)).build()

      implicit val msgs: Messages = messages(application)

      running(application) {
        implicit val request =
          FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersForAgentsView]
        val list = ApplicationSummary(userAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilderAsAgent(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Application Complete when application submission succeeds" in {

      val mockBackendConnector = mock[BackendConnector]
      val application          = applicationBuilderAsAgent(Option(userAnswers))
        .overrides(bind[BackendConnector].to(mockBackendConnector))
        .build()

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(
        mockBackendConnector.submitAnswers(userAnswersCaptor.capture())(any(), any())
      ) thenReturn Future.successful(Right(HttpResponse(status = OK, body = "success")))

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersForAgentsController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ApplicationCompleteController
          .onPageLoad(userAnswers.applicationNumber)
          .url
        userAnswersCaptor.getValue mustEqual userAnswers
      }
    }

    "must redirect to Journey Recovery when application submission fails" in {

      val mockBackendConnector = mock[BackendConnector]
      val application          = applicationBuilderAsAgent(Option(userAnswers))
        .overrides(bind[BackendConnector].to(mockBackendConnector))
        .build()

      when(
        mockBackendConnector.submitAnswers(any())(any(), any())
      ) thenReturn Future.successful(Left(BackendError(INTERNAL_SERVER_ERROR, "backend error")))

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersForAgentsController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
