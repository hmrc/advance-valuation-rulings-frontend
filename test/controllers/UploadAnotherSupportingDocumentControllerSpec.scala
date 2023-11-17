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
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import navigation.{FakeNavigator, Navigator}
import pages.UploadLetterOfAuthorityPage
import play.api.Configuration
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.DraftAttachmentAt
import views.html.UploadAnotherSupportingDocumentView

import java.time.Instant
import scala.concurrent.Future

class UploadAnotherSupportingDocumentControllerSpec extends SpecBase {

  private val formProvider = new UploadAnotherSupportingDocumentFormProvider(
    Configuration("upscan.maxFiles" -> 5)
  )
  private val form         = formProvider(Seq.empty)

  private lazy val uploadAnotherSupportingDocumentRoute =
    routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, draftId).url

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

  "UploadAnotherSupportingDocument Controller" - {

    "must return OK and the correct view for a GET" in {

      val answers: UserAnswers = userAnswersAsIndividualTrader
        .set(DraftAttachmentAt(Index(0)), DraftAttachment(successfulFile, Some(false)))
        .success
        .value

      val attachments = Seq(
        DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
      )

      val application            = applicationBuilder(userAnswers = Some(answers)).build()
      val request                = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)
      val result: Future[Result] = route(application, request).value
      val view                   = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(attachments, form, NormalMode, draftId)(
        request,
        messages(application)
      ).toString
    }

    "must return OK and the correct view for a GET with a letter of authority" in {

      val answers: UserAnswers = userAnswersAsIndividualTrader
        .set(DraftAttachmentAt(Index(0)), DraftAttachment(successfulFile, Some(false)))
        .success
        .value
        .set(UploadLetterOfAuthorityPage, successfulFile)
        .success
        .value

      val attachments = Seq(
        DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
      )

      val application            = applicationBuilder(userAnswers = Some(answers)).build()
      val request                = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)
      val result: Future[Result] = route(application, request).value
      val view                   = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        attachments,
        form,
        NormalMode,
        draftId,
        successfulFile.fileName
      )(
        request,
        messages(application)
      ).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      val request =
        FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
          .withFormUrlEncodedBody("value" -> "true")
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()
      val request     =
        FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
          .withFormUrlEncodedBody(("value", ""))
      val boundForm   = form.bind(Map("value" -> ""))
      val view        = application.injector.instanceOf[UploadAnotherSupportingDocumentView]
      val result      = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(Seq.empty, boundForm, NormalMode, draftId)(
        request,
        messages(application)
      ).toString
    }

    "must return a Bad Request and errors when invalid data is submitted with a letter of authority" in {

      val answers: UserAnswers = userAnswersAsIndividualTrader
        .set(UploadLetterOfAuthorityPage, successfulFile)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(answers)).build()
      val request     =
        FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
          .withFormUrlEncodedBody(("value", ""))
      val boundForm   = form.bind(Map("value" -> ""))
      val view        = application.injector.instanceOf[UploadAnotherSupportingDocumentView]
      val result      = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(
        Seq.empty,
        boundForm,
        NormalMode,
        draftId,
        successfulFile.fileName
      )(
        request,
        messages(application)
      ).toString
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)
      val result      = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()
      val request     =
        FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
          .withFormUrlEncodedBody(("value", "true"))
      val result      = route(application, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
    }
  }
}
