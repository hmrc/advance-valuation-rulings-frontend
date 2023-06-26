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

import java.time.Instant

import scala.concurrent.Future

import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.IsThisFileConfidentialFormProvider
import models.{Done, Index, NormalMode, UploadedFile}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{IsThisFileConfidentialPage, UploadSupportingDocumentPage}
import services.UserAnswersService
import views.html.IsThisFileConfidentialView

class IsThisFileConfidentialControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/foo")

  private val formProvider = new IsThisFileConfidentialFormProvider()
  private val form         = formProvider()

  private val successfulFile = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1337
    )
  )

  private lazy val isThisFileConfidentialRoute =
    routes.IsThisFileConfidentialController.onPageLoad(Index(0), NormalMode, draftId).url

  private val userAnswers =
    userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage(Index(0)), successfulFile)
      .success
      .value

  "IsThisFileConfidential Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val result      = route(application, request).value
      val view        = application.injector.instanceOf[IsThisFileConfidentialView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, Index(0), NormalMode, draftId)(
        request,
        messages(application)
      ).toString
    }

    "must populate the form on a GET when the question has previously been answered" in {

      val answers     = userAnswers.set(IsThisFileConfidentialPage(Index(0)), true).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val view        = application.injector.instanceOf[IsThisFileConfidentialView]
      val result      = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        form.fill(true),
        Index(0),
        NormalMode,
        draftId
      )(
        request,
        messages(application)
      ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]
      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      val request =
        FakeRequest(POST, isThisFileConfidentialRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()
      val request     =
        FakeRequest(POST, isThisFileConfidentialRoute)
          .withFormUrlEncodedBody(("value", ""))
      val boundForm   = form.bind(Map("value" -> ""))
      val view        = application.injector.instanceOf[IsThisFileConfidentialView]
      val result      = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, Index(0), NormalMode, draftId)(
        request,
        messages(application)
      ).toString
      verify(mockUserAnswersService, times(1)).set(any())(any())
    }

    "must return NOT_FOUND when the index is greater than the maximum number of files a user is allowed to upload" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .configure(
          "upscan.maxFiles" -> 1
        )
        .build()

      val request = FakeRequest(
        controllers.routes.IsThisFileConfidentialController
          .onPageLoad(Index(1), models.NormalMode, draftId)
      )

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND
    }

    "must return NOT_FOUND when the index is greater than the greatest existing index + 1" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .configure(
          "upscan.maxFiles" -> 5
        )
        .build()

      val request = FakeRequest(
        controllers.routes.IsThisFileConfidentialController
          .onPageLoad(Index(1), models.NormalMode, draftId)
      )

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND
    }

    "must return NOT_FOUND when the index is greater than the maximum number of files a user is allowed to upload for POST" in {
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .configure(
          "upscan.maxFiles" -> 1
        )
        .build()

      val request = FakeRequest(
        controllers.routes.IsThisFileConfidentialController
          .onSubmit(Index(1), models.NormalMode, draftId)
      )

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND
    }

    "must return NOT_FOUND when the index is greater than the greatest existing index + 1 for POST" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .configure(
          "upscan.maxFiles" -> 5
        )
        .build()

      val request = FakeRequest(
        controllers.routes.IsThisFileConfidentialController
          .onSubmit(Index(1), models.NormalMode, draftId)
      )

      val result = route(application, request).value

      status(result) mustEqual NOT_FOUND
    }

    "must redirect to UploadSupportingDocument page if the file is not successful" in {

      val failedFile  = UploadedFile.Failure(
        reference = "reference",
        failureDetails = UploadedFile.FailureDetails(
          failureReason = UploadedFile.FailureReason.Quarantine,
          failureMessage = Some("failureMessage")
        )
      )
      val answers     =
        userAnswersAsIndividualTrader
          .set(UploadSupportingDocumentPage(Index(0)), failedFile)
          .success
          .value
      val application = applicationBuilder(userAnswers = Some(answers)).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val result      = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UploadSupportingDocumentsController
        .onPageLoad(Index(0), NormalMode, draftId, None, None)
        .url
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val result      = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     =
        FakeRequest(POST, isThisFileConfidentialRoute)
          .withFormUrlEncodedBody(("value", "true"))
      val result      = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
