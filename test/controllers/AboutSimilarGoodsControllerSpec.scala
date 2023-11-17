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

import base.SpecBase
import forms.AboutSimilarGoodsFormProvider
import models.{Done, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import pages.AboutSimilarGoodsPage
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.AboutSimilarGoodsView

import scala.concurrent.Future

class AboutSimilarGoodsControllerSpec extends SpecBase {

  lazy val getRoute: String  = routes.AboutSimilarGoodsController.onPageLoad(NormalMode, draftId).url
  lazy val postRoute: String =
    routes.AboutSimilarGoodsController.onSubmit(NormalMode, draftId, saveDraft = false).url

  val formProvider = new AboutSimilarGoodsFormProvider()
  val form         = formProvider()

  "AboutSimilarGoodsController" - {
    "onPageLoad" - {
      "must return OK when no answers" in {

        val app = applicationBuilder(Some(userAnswersAsIndividualTrader)).build()
        running(app) {
          val request = FakeRequest(GET, getRoute)

          val result = route(app, request).value

          val view = app.injector.instanceOf[AboutSimilarGoodsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, draftId)(
            request,
            messages(app)
          ).toString
        }
      }

      "must prepopulate the dialog if already answered" in {

        val userAnswers =
          userAnswersAsIndividualTrader.set(AboutSimilarGoodsPage, "test string").get

        val app = applicationBuilder(Some(userAnswers)).build()
        running(app) {
          val request = FakeRequest(GET, getRoute)

          val result = route(app, request).value

          val view = app.injector.instanceOf[AboutSimilarGoodsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider().fill("test string"),
            NormalMode,
            draftId
          )(
            request,
            messages(app)
          ).toString
        }
      }

    }

    "onSubmit" - {
      "must redirect to onPageLoad if no data submitted" in {

        val mockUserAnswersService = mock[UserAnswersService]
        when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

        val application = applicationBuilder(Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, postRoute)

          val view = application.injector.instanceOf[AboutSimilarGoodsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            form.bind(Map("value" -> "")),
            NormalMode,
            draftId
          )(
            request,
            messages(application)
          ).toString
        }
      }

      "must redirect to HasCommodityCodeController when data submitted" in {
        val mockUserAnswersService = mock[UserAnswersService]
        when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

        val application = applicationBuilder(Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

        running(application) {
          val request = FakeRequest(POST, postRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.routes.HasCommodityCodeController.onPageLoad(NormalMode, draftId).url
          )
        }
      }
    }
  }
}
