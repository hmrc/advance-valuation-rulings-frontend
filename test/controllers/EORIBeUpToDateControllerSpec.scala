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
import models.CheckRegisteredDetails
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CheckRegisteredDetailsPage
import services.UserAnswersService
import views.html.EORIBeUpToDateView

class EORIBeUpToDateControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val checkRegisteredDetailRoute           =
    routes.CheckRegisteredDetailsController.onPageLoad(models.NormalMode, draftId).url
  val registeredDetails: CheckRegisteredDetails = CheckRegisteredDetails(
    value = false,
    eori = "GB123456789012345",
    consentToDisclosureOfPersonalData = true,
    name = "Test Name",
    streetAndNumber = "Test Street 1",
    city = "Test City",
    country = "Test Country",
    postalCode = Some("Test Postal Code"),
    phoneNumber = Some("Test Telephone Number")
  )

  "EORIBeUpToDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.EORIBeUpToDateController.onPageLoad(draftId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EORIBeUpToDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(draftId)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when yes is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      val userAnswers = emptyUserAnswers
        .set(CheckRegisteredDetailsPage, registeredDetails)
        .success
        .value

      when(mockUserAnswersService.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.get(any(), any())) thenReturn Future.successful(Some(userAnswers))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when 'no' is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]
      val userAnswers            = emptyUserAnswers
        .set(CheckRegisteredDetailsPage, registeredDetails)
        .success
        .value

      when(mockUserAnswersService.set(any())) thenReturn Future.successful(true)
      when(mockUserAnswersService.get(any(), any())) thenReturn Future.successful(Some(userAnswers))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
