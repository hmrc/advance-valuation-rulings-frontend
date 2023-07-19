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
import connectors.BackendConnector
import forms.TraderEoriNumberFormProvider
import models.{BackendError, Done, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ProvideTraderEoriPage
import services.UserAnswersService
import views.html.{InvalidTraderEoriView, ProvideTraderEoriView}

class ProvideTraderEoriControllerSpec extends SpecBase with MockitoSugar {

  override def onwardRoute = Call("GET", s"/advance-valuation-ruling/$draftId/verify-trader-eori")

  lazy val provideTraderEoriPageRoute     =
    routes.ProvideTraderEoriController.onPageLoad(draftId).url
  lazy val provideTraderEoriPagePostRoute =
    routes.ProvideTraderEoriController.onSubmit(draftId).url

  val mockBackendConnector = mock[BackendConnector]
  val formProvider         = new TraderEoriNumberFormProvider()
  val form                 = formProvider()

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
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, provideTraderEoriPagePostRoute)
            .withFormUrlEncodedBody(
              "value" -> "GB24567FD6GHF788"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must display an error on the page when no EORI number is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

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
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ProvideTraderEoriView]

        val boundForm =
          form.bind(Map("value" -> ""))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
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
        val eoriNo  = "GBS0M330RI"
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
              "value" -> "GB345"
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}