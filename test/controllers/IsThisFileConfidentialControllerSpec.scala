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
import forms.IsThisFileConfidentialFormProvider
import models.{Done, DraftAttachment, NormalMode, UploadedFile}
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, verify, when}
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.AllDocuments
import services.UserAnswersService
import views.html.IsThisFileConfidentialView

import java.time.Instant
import scala.concurrent.Future

class IsThisFileConfidentialControllerSpec extends SpecBase {

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
    routes.IsThisFileConfidentialController.onPageLoad(NormalMode, draftId).url

  private val userAnswers =
    userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

  "IsThisFileConfidential Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val result      = route(application, request).value
      val view        = application.injector.instanceOf[IsThisFileConfidentialView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form, NormalMode, draftId, "fileName")(
        request,
        messages(application)
      ).toString
    }

    "must populate the form on a GET when the question has previously been answered" in {

      val answers     = userAnswers.set(IsThisFileConfidentialPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val view        = application.injector.instanceOf[IsThisFileConfidentialView]
      val result      = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        form.fill(true),
        NormalMode,
        draftId,
        "fileName"
      )(
        request,
        messages(application)
      ).toString
    }

    "when valid data is submitted" - {
      val mockUserAnswersService = mock(classOf[UserAnswersService])
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      val request =
        FakeRequest(POST, isThisFileConfidentialRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      "must redirect to the next page" in {
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

      "must promote the draft attachment to AllDocuments" in {
        val expectedUserAnswers = userAnswersAsIndividualTrader
          .set(AllDocuments, List(DraftAttachment(successfulFile, Some(true))))
          .success
          .value

        verify(mockUserAnswersService).set(eqTo(expectedUserAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()
      val request     =
        FakeRequest(POST, isThisFileConfidentialRoute)
          .withFormUrlEncodedBody(("value", ""))
      val boundForm   = form.bind(Map("value" -> ""))
      val view        = application.injector.instanceOf[IsThisFileConfidentialView]
      val result      = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(boundForm, NormalMode, draftId, "")(
        request,
        messages(application)
      ).toString
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
          .set(UploadSupportingDocumentPage, failedFile)
          .success
          .value
      val application = applicationBuilder(userAnswers = Some(answers)).build()
      val request     = FakeRequest(GET, isThisFileConfidentialRoute)
      val result      = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UploadSupportingDocumentsController
        .onPageLoad(NormalMode, draftId, None, None)
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
