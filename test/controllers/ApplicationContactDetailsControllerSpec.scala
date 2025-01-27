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

package controllers

import base.SpecBase
import forms.ApplicationContactDetailsFormProvider
import models.{ApplicationContactDetails, NormalMode}
import pages.ApplicationContactDetailsPage
import play.api.Application
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ApplicationContactDetailsView

class ApplicationContactDetailsControllerSpec extends SpecBase {

  private val formProvider = new ApplicationContactDetailsFormProvider()
  private val form         = formProvider()

  private lazy val applicationContactDetailsRoute: String =
    routes.ApplicationContactDetailsController.onPageLoad(NormalMode, draftId).url
  private lazy val saveDraftRoute: String                 =
    routes.ApplicationContactDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = true)
      .url

  private lazy val continueRoute: String =
    routes.ApplicationContactDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = false)
      .url
  "ApplicationContactDetails Controller" - {

    "Redirects to Draft saved page when save-draft is selected" in {

      val application: Application = setupTestBuild(userAnswersAsIndividualTrader)

      running(application) {
        val request =
          FakeRequest(POST, saveDraftRoute)
            .withFormUrlEncodedBody(
              ("name", "my name"),
              ("email", "email@example.co.uk"),
              ("phone", "07123456789"),
              ("jobTitle", "CEO")
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
        val request = FakeRequest(GET, applicationContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ApplicationContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val applicationContactDetails = ApplicationContactDetails(
        name = "my name",
        email = "email@example.co.uk",
        phone = "07123456789",
        jobTitle = "CEO"
      )

      val userAnswers = userAnswersAsIndividualTrader
        .set(ApplicationContactDetailsPage, applicationContactDetails)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, applicationContactDetailsRoute)

        val view = application.injector.instanceOf[ApplicationContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(applicationContactDetails),
          NormalMode,
          draftId
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application: Application = setupTestBuild(userAnswersAsIndividualTrader)
      running(application) {
        val request =
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(
              ("name", "my name"),
              ("email", "email@example.co.uk"),
              ("phone", "07123456789"),
              ("jobTitle", "CEO")
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
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ApplicationContactDetailsView]

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
        val request = FakeRequest(GET, applicationContactDetailsRoute)

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
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
