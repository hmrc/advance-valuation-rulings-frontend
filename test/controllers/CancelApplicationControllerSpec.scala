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
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.CancelAreYouSureView

import scala.concurrent.Future

class CancelApplicationControllerSpec extends SpecBase {

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
  }
}
