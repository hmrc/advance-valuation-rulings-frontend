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
import connectors.BackendConnector
import forms.TraderEoriNumberFormProvider
import models.{BackendError, Done, NormalMode}
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import pages.ProvideTraderEoriPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.{InvalidTraderEoriView, ProvideTraderEoriView}

import scala.concurrent.Future

class ProvideTraderEoriControllerSpec extends SpecBase {

  private lazy val provideTraderEoriPageRoute     =
    routes.ProvideTraderEoriController.onPageLoad(NormalMode, draftId).url
  private lazy val provideTraderEoriPagePostRoute =
    routes.ProvideTraderEoriController.onSubmit(NormalMode, draftId, saveDraft = false).url

  private val mockBackendConnector = mock[BackendConnector]
  private val formProvider         = new TraderEoriNumberFormProvider()
  private val form                 = formProvider()

  "ProvideTraderEoriController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, provideTraderEoriPageRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProvideTraderEoriView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val eoriNo      = "GB1234GFDFHG56"
      val userAnswers = userAnswersAsIndividualTrader
        .set(ProvideTraderEoriPage, eoriNo)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, provideTraderEoriPageRoute)

        val view = application.injector.instanceOf[ProvideTraderEoriView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(eoriNo),
          NormalMode,
          draftId
        )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBackendConnector.getTraderDetails(any(), any())(any(), any())) thenReturn Future
        .successful(Right(traderDetailsWithCountryCode))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, provideTraderEoriPagePostRoute)
            .withFormUrlEncodedBody(
              "value" -> "GB123456123456"
            )

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must display an error on the page when" - {

      "no EORI number is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, provideTraderEoriPagePostRoute)
              .withFormUrlEncodedBody(("value", ""))

          val result = route(application, request).value

          val view      = application.injector.instanceOf[ProvideTraderEoriView]
          val boundForm =
            form.bind(Map("value" -> ""))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
            request,
            messages(application)
          ).toString
        }
      }

      "value submitted is too long" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, provideTraderEoriPagePostRoute)
              .withFormUrlEncodedBody(("value", "GB123123123123123"))

          val result = route(application, request).value

          val view      = application.injector.instanceOf[ProvideTraderEoriView]
          val boundForm =
            form.bind(Map("value" -> "GB123123123123123"))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
            request,
            messages(application)
          ).toString
        }
      }

      "value submitted is too short" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, provideTraderEoriPagePostRoute)
              .withFormUrlEncodedBody(("value", "GB123123123"))

          val result = route(application, request).value

          val view      = application.injector.instanceOf[ProvideTraderEoriView]
          val boundForm =
            form.bind(Map("value" -> "GB123123123"))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
            request,
            messages(application)
          ).toString
        }
      }

      "value submitted does not start with GB" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, provideTraderEoriPagePostRoute)
              .withFormUrlEncodedBody(("value", "AB123123123123"))

          val result = route(application, request).value

          val view      = application.injector.instanceOf[ProvideTraderEoriView]
          val boundForm =
            form.bind(Map("value" -> "AB123123123123"))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
            request,
            messages(application)
          ).toString
        }
      }

      "value submitted contains special characters" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, provideTraderEoriPagePostRoute)
              .withFormUrlEncodedBody(("value", "GB1231231!!!!3"))

          val result = route(application, request).value

          val view      = application.injector.instanceOf[ProvideTraderEoriView]
          val boundForm =
            form.bind(Map("value" -> "GB1231231!!!!3"))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
            request,
            messages(application)
          ).toString
        }
      }

      "value does not otherwise match the format" in {
        val application =
          applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, provideTraderEoriPagePostRoute)
              .withFormUrlEncodedBody(("value", "GB123123ABCABC"))

          val result = route(application, request).value

          val view      = application.injector.instanceOf[ProvideTraderEoriView]
          val boundForm =
            form.bind(Map("value" -> "GB123123ABCABC"))

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
            request,
            messages(application)
          ).toString
        }
      }
    }

    "must return invalidEoriView for a POST if provided EORI is not found" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBackendConnector.getTraderDetails(any(), any())(any(), any())) thenReturn Future
        .successful(Left(BackendError(404, "eori not found")))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val eoriNo  = "GB123123123123"
        val request =
          FakeRequest(POST, provideTraderEoriPagePostRoute)
            .withFormUrlEncodedBody(
              "value" -> eoriNo
            )

        val result = route(application, request).value

        val view = application.injector.instanceOf[InvalidTraderEoriView]

        status(result) mustEqual NOT_FOUND
        contentAsString(result) mustEqual view(NormalMode, draftId, eoriNo)(
          request,
          messages(application)
        ).toString

      }
    }

    "must redirect to Journey Recovery for a POST if some other exception is returned by the backend connector" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)
      when(mockBackendConnector.getTraderDetails(any(), any())(any(), any())) thenReturn Future
        .successful(Left(BackendError(code = 500, message = "some backend error")))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, provideTraderEoriPagePostRoute)
            .withFormUrlEncodedBody(
              "value" -> "GB123123123123"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
