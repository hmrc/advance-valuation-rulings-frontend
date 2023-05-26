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
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import services.UserAnswersService
import views.html.CancelAreYouSureView

class CancelApplicationControllerSpec extends SpecBase with MockitoSugar {

  "CancelApplication Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CancelApplicationController.onPageLoad(draftId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancelAreYouSureView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(draftId)(request, messages(application)).toString
      }
    }

    "must clear answers and redirect" in {

      val mockUserAnswersService = mock[UserAnswersService]

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

    "must clear answers and redirect when the user selects 'Yes'" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.clear(any())(any())).thenReturn(Future.successful(Done))

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.CancelApplicationController.onSubmit(draftId).url)
            .withFormUrlEncodedBody(("value", "true"))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersService, times(1)).clear(eqTo(draftId))(any())
      }
    }

     "must not clear answers when the user selects 'No'" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.clear(any())(any())).thenReturn(Future.successful(Done))

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.CancelApplicationController.onSubmit(draftId).url)
            .withFormUrlEncodedBody(("value", "false"))
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockUserAnswersService, times(0)).clear(eqTo(draftId))(any())
      }
    }
  }
}
