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
import forms.ImportGoodsFormProvider
import models.{Done, NormalMode}
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import pages.ImportGoodsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UserAnswersService
import views.html.ImportGoodsView

import scala.concurrent.Future

class ImportGoodsControllerSpec extends SpecBase {

  val formProvider        = new ImportGoodsFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val importGoodsRoute: String = routes.ImportGoodsController.onPageLoad(NormalMode, draftId).url

  "ImportGoods Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, importGoodsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ImportGoodsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersAsIndividualTrader.set(ImportGoodsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, importGoodsRoute)

        val view = application.injector.instanceOf[ImportGoodsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, importGoodsRoute)
            .withFormUrlEncodedBody(("value", "true"))

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
          FakeRequest(POST, importGoodsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ImportGoodsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, importGoodsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" ignore {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, importGoodsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
