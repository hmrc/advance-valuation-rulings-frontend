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

import base.SpecBase
import forms.WhyIdenticalGoodsFormProvider
import models.NormalMode
import pages.WhyIdenticalGoodsPage
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhyIdenticalGoodsView

class WhyIdenticalGoodsControllerSpec extends SpecBase {

  val formProvider = new WhyIdenticalGoodsFormProvider()
  val form         = formProvider()

  lazy val whyIdenticalGoodsRoute =
    routes.WhyIdenticalGoodsController.onPageLoad(NormalMode, draftId).url

  lazy val saveDraftRoute: String =
    routes.WhyIdenticalGoodsController.onSubmit(NormalMode, draftId, saveDraft = true).url

  lazy val continueRoute: String =
    routes.WhyIdenticalGoodsController.onSubmit(NormalMode, draftId, saveDraft = false).url

  "WhyIdenticalGoods Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, whyIdenticalGoodsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhyIdenticalGoodsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersAsIndividualTrader
          .set(WhyIdenticalGoodsPage, "answer")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whyIdenticalGoodsRoute)

        val view = application.injector.instanceOf[WhyIdenticalGoodsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, draftId)(
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
            .withFormUrlEncodedBody(("value", "answer"))

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

        val view = application.injector.instanceOf[WhyIdenticalGoodsView]

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
        val request = FakeRequest(GET, whyIdenticalGoodsRoute)

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
