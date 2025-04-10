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
import forms.VerifyTraderDetailsFormProvider
import models.{Done, NormalMode, TraderDetailsWithConfirmation}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import pages.{CheckRegisteredDetailsPage, VerifyTraderDetailsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.{VerifyPrivateTraderDetailView, VerifyPublicTraderDetailView}

import scala.concurrent.Future

class VerifyTraderEoriControllerSpec extends SpecBase {

  private lazy val verifyTraderEoriPageRoute     =
    routes.VerifyTraderEoriController.onPageLoad(NormalMode, draftId).url
  private lazy val verifyTraderEoriPagePostRoute =
    routes.VerifyTraderEoriController.onSubmit(NormalMode, draftId).url

  private val formProvider = new VerifyTraderDetailsFormProvider()

  "VerifyTraderEoriController" - {

    "must return OK and the public view when trader consents to disclose info" in {

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verifyTraderEoriPageRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyPublicTraderDetailView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          formProvider(Some(traderDetailsWithConfirmation)),
          NormalMode,
          draftId,
          traderDetailsWithConfirmation
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the private view when trader does not consent to disclose info" in {

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation.copy(consentToDisclosureOfPersonalData = false)
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verifyTraderEoriPageRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyPrivateTraderDetailView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          formProvider(Some(traderDetailsWithConfirmation)),
          NormalMode,
          draftId,
          traderDetailsWithConfirmation
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must prepopulate the dialog if confirmation has already been provided" in {
      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation.copy(confirmation = Some(true))
        )
        .futureValue
        .setFuture[Boolean](
          CheckRegisteredDetailsPage,
          true
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, verifyTraderEoriPageRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyPublicTraderDetailView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(
          formProvider(Some(traderDetailsWithConfirmation)).fill("true"),
          NormalMode,
          draftId,
          traderDetailsWithConfirmation
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to recovery page when no trader details in session" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, verifyTraderEoriPageRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.JourneyRecoveryController
            .onPageLoad(
              continueUrl = Some(
                RedirectUrl(
                  controllers.routes.ProvideTraderEoriController.onPageLoad(NormalMode, draftId).url
                )
              )
            )
            .url
        )
      }
    }

    "must redirect to upload letter of authority when private and approved" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation.copy(consentToDisclosureOfPersonalData = false)
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, verifyTraderEoriPagePostRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.UploadLetterOfAuthorityController
            .onPageLoad(NormalMode, draftId, None, None, redirectedFromChangeButton = false)
            .url
        )
      }
    }

    "must redirect to upload letter of authority when public and approved" in {
      val mockUserAnswersService = mock(classOf[UserAnswersService])
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation.copy(consentToDisclosureOfPersonalData = true)
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, verifyTraderEoriPagePostRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.UploadLetterOfAuthorityController
            .onPageLoad(NormalMode, draftId, None, None, redirectedFromChangeButton = false)
            .url
        )
      }
    }

    "must redirect to Kickout Page when public and unapproved" in {
      val mockUserAnswersService = mock(classOf[UserAnswersService])
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation.copy(consentToDisclosureOfPersonalData = true)
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, verifyTraderEoriPagePostRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.EORIBeUpToDateController.onPageLoad(draftId).url
        )
      }
    }

    "must redirect to Kickout Page when private and unapproved" in {
      val mockUserAnswersService = mock(classOf[UserAnswersService])
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation.copy(consentToDisclosureOfPersonalData = true)
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request = FakeRequest(POST, verifyTraderEoriPagePostRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.routes.EORIBeUpToDateController.onPageLoad(draftId).url
        )
      }
    }

    "must display an error on the page when no selection is made - public" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])

      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          traderDetailsWithConfirmation
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verifyTraderEoriPagePostRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyPublicTraderDetailView]

        val boundForm =
          formProvider(Some(traderDetailsWithConfirmation)).bind(Map("value" -> ""))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          draftId,
          traderDetailsWithConfirmation
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must display an error on the page when no selection is made - private" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])

      val details     = traderDetailsWithConfirmation.copy(consentToDisclosureOfPersonalData = false)
      val userAnswers = userAnswersAsIndividualTrader
        .setFuture[TraderDetailsWithConfirmation](
          VerifyTraderDetailsPage,
          details
        )
        .futureValue

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, verifyTraderEoriPagePostRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[VerifyPrivateTraderDetailView]

        val boundForm =
          formProvider(Some(details)).bind(Map("value" -> ""))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, draftId, details)(
          request,
          messages(application)
        ).toString
      }
    }

  }
}
