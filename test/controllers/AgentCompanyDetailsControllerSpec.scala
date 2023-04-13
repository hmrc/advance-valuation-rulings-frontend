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

import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.AgentCompanyDetailsFormProvider
import models.{AgentCompanyDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentCompanyDetailsPage
import repositories.SessionRepository
import views.html.AgentCompanyDetailsView

class AgentCompanyDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AgentCompanyDetailsFormProvider()
  val form         = formProvider()

  lazy val agentCompanyDetailsRoute =
    routes.AgentCompanyDetailsController.onPageLoad(NormalMode).url

  val userAnswers = UserAnswers(
    userAnswersId,
    draftId,
    Json.obj(
      AgentCompanyDetailsPage.toString -> Json.obj(
        "agentEori"            -> "GB12341234123",
        "agentCompanyName"     -> "companyName",
        "agentStreetAndNumber" -> "streetandNumber",
        "agentCity"            -> "city",
        "agentCountry"         -> "country"
      )
    )
  )

  "AgentCompanyDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentCompanyDetailsRoute)

        val view = application.injector.instanceOf[AgentCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, agentCompanyDetailsRoute)

        val view = application.injector.instanceOf[AgentCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        val agentCompanyDetails =
          AgentCompanyDetails(
            "GB12341234123",
            "companyName",
            "streetandNumber",
            "city",
            "country",
            None
          )
        contentAsString(result) mustEqual view(form.fill(agentCompanyDetails), NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, agentCompanyDetailsRoute)
            .withFormUrlEncodedBody(
              ("agentEori", "GB12341234123"),
              ("agentCompanyName", "value 2"),
              ("agentStreetAndNumber", "streetandNumber"),
              ("agentCity", "city"),
              ("agentCountry", "country")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, agentCompanyDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AgentCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, agentCompanyDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, agentCompanyDetailsRoute)
            .withFormUrlEncodedBody(
              ("agentEori", "GB12341234123"),
              ("agentCompanyName", "value 2"),
              ("agentStreetAndNumber", "streetandNumber"),
              ("agentCity", "city"),
              ("agentCountry", "country")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}