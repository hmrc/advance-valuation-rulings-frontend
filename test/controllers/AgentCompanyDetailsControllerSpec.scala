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

import play.api.Application
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.AgentCompanyDetailsFormProvider
import models.{AgentCompanyDetails, Country, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import pages.AgentCompanyDetailsPage
import views.html.AgentCompanyDetailsView

class AgentCompanyDetailsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AgentCompanyDetailsFormProvider()
  val form         = formProvider()

  lazy val agentCompanyDetailsRoute =
    routes.AgentCompanyDetailsController.onPageLoad(NormalMode, draftId).url
  lazy val saveDraftRoute: String   =
    routes.AgentCompanyDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = true)
      .url

  lazy val continueRoute: String =
    routes.AgentCompanyDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = false)
      .url
  val agentCompanyDetails        =
    AgentCompanyDetails(
      "GB12341234123",
      "companyName",
      "streetandNumber",
      "city",
      Country("GB", "United Kingdom"),
      None
    )

  val userAnswers =
    userAnswersAsIndividualTrader.set(AgentCompanyDetailsPage, agentCompanyDetails).success.value

  "AgentCompanyDetails Controller" - {

    "Redirects to Draft saved page when save-draft is selected" in {

      val application: Application = setupTestBuild(userAnswersAsIndividualTrader)

      running(application) {
        val request =
          FakeRequest(POST, saveDraftRoute)
            .withFormUrlEncodedBody(
              ("agentEori", "GB12341234123"),
              ("agentCompanyName", "value 2"),
              ("agentStreetAndNumber", "streetandNumber"),
              ("agentCity", "city"),
              ("country", "GB"),
              ("agentPostalCode", "AA1 1AA")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Call(
          "POST",
          s"/advance-valuation-ruling/$draftId/save-as-draft"
        ).url
      }
    }
    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, agentCompanyDetailsRoute)

        val view = application.injector.instanceOf[AgentCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
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

        contentAsString(result) mustEqual view(form.fill(agentCompanyDetails), NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application: Application = setupTestBuild(userAnswersAsIndividualTrader)
      running(application) {
        val request =
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(
              ("agentEori", "GB12341234123"),
              ("agentCompanyName", "value 2"),
              ("agentStreetAndNumber", "streetandNumber"),
              ("agentCity", "city"),
              ("country", "GB"),
              ("agentPostalCode", "AA1 1AA")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request =
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AgentCompanyDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
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
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(
              ("agentEori", "GB12341234123"),
              ("agentCompanyName", "value 2"),
              ("agentStreetAndNumber", "streetandNumber"),
              ("agentCity", "city"),
              ("agentCountry", "GB"),
              ("agentPostalCode", "AA1 1AA")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
