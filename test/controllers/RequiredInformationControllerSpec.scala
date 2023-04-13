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
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import forms.RequiredInformationFormProvider
import models.RequiredInformation
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.RequiredInformationPage
import repositories.SessionRepository
import views.html.RequiredInformationView

class RequiredInformationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val requiredInformationRoute =
    routes.RequiredInformationController.onPageLoad().url

  val formProvider = new RequiredInformationFormProvider()
  val form         = formProvider()

  "RequiredInformation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, requiredInformationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequiredInformationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, AffinityGroup.Individual)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(RequiredInformationPage, RequiredInformation.values.toSet)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, requiredInformationRoute)

        val view = application.injector.instanceOf[RequiredInformationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(RequiredInformation.values.toSet),
          AffinityGroup.Individual
        )(request, messages(application)).toString
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
          FakeRequest(POST, requiredInformationRoute)
            .withFormUrlEncodedBody(
              "value[0]" -> RequiredInformation.Option1.toString,
              "value[1]" -> RequiredInformation.Option2.toString,
              "value[2]" -> RequiredInformation.Option3.toString,
              "value[3]" -> RequiredInformation.Option4.toString,
              "value[4]" -> RequiredInformation.Option5.toString,
              "value[5]" -> RequiredInformation.Option6.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
    "must display an error on the page when not all checkbox are submitted" in {

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
          FakeRequest(POST, requiredInformationRoute)
            .withFormUrlEncodedBody(("value[0]", RequiredInformation.values.head.toString))

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequiredInformationView]

        val boundForm =
          form.bind(Map("value[0]" -> RequiredInformation.values.head.toString))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, AffinityGroup.Individual)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, requiredInformationRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RequiredInformationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, AffinityGroup.Individual)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, requiredInformationRoute)
            .withFormUrlEncodedBody(
              "value[0]" -> RequiredInformation.Option1.toString,
              "value[1]" -> RequiredInformation.Option2.toString,
              "value[2]" -> RequiredInformation.Option3.toString,
              "value[3]" -> RequiredInformation.Option4.toString,
              "value[4]" -> RequiredInformation.Option5.toString,
              "value[5]" -> RequiredInformation.Option6.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
