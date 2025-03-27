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
import forms.CancelApplicationFormProvider
import models.Done
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UserAnswersService
import views.html.CancelAreYouSureView
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator

import scala.concurrent.Future

class CancelApplicationControllerSpec extends SpecBase {

  val formProvider                        = new CancelApplicationFormProvider()
  val form: Form[Boolean]                 = formProvider()
  lazy val cancelApplicationRoute: String = routes.CancelApplicationController.onPageLoad(draftId).url

  "CancelApplication Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CancelApplicationController.onPageLoad(draftId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancelAreYouSureView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, draftId)(request, messages(application)).toString
      }
    }

    "must clear answers and redirect" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])

      reset(mockUserAnswersService)

      when(mockUserAnswersService.clear(any())(any())).thenReturn(Future.successful(Done))

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
        .build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CancelApplicationController.confirmCancel(draftId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersService, times(1)).clear(eqTo(draftId))(any())
      }
    }
    "must redirect to the next page when yes is submitted" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])

      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cancelApplicationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when no is submitted" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])

      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cancelApplicationRoute)
            .withFormUrlEncodedBody(("value", "false"))

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
          FakeRequest(POST, cancelApplicationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[CancelAreYouSureView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, draftId)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
