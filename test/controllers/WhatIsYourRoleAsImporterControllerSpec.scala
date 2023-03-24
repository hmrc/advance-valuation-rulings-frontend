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
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.WhatIsYourRoleAsImporterFormProvider
import models.WhatIsYourRoleAsImporter
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.WhatIsYourRoleAsImporterPage
import repositories.SessionRepository
import views.html.WhatIsYourRoleAsImporterView

class WhatIsYourRoleAsImporterControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val whatIsYourRoleAsImporterRoute =
    routes.WhatIsYourRoleAsImporterController.onPageLoad().url

  val formProvider = new WhatIsYourRoleAsImporterFormProvider()
  val form         = formProvider()

  "WhatIsYourRoleAsImporter Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilderAsAgent(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourRoleAsImporterRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsYourRoleAsImporterView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.values.head)
        .success
        .value

      val application = applicationBuilderAsAgent(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourRoleAsImporterRoute)

        val view = application.injector.instanceOf[WhatIsYourRoleAsImporterView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(WhatIsYourRoleAsImporter.values.head)
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilderAsAgent(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilderAsAgent(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhatIsYourRoleAsImporterView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(
          request,
          messages(application)
        ).toString
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
