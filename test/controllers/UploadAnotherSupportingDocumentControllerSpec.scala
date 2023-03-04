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
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import models.fileupload.{UploadId, UpscanFileDetails}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UploadSupportingDocumentPage
import repositories.SessionRepository
import views.html.UploadAnotherSupportingDocumentView

class UploadAnotherSupportingDocumentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val numOfDocs                          = "one"
  val upscanFileDeets: UpscanFileDetails = UpscanFileDetails(UploadId("id"), "name", "some.url")

  val uploadedFiles                   = UploadedFiles.initialise(upscanFileDeets)
  val uploadedFileWithConfidentiality = uploadedFiles.setConfidentiality(true)

  val formProvider = new UploadAnotherSupportingDocumentFormProvider()
  val form         = formProvider()
  val link         = new views.html.components.Link()

  lazy val uploadAnotherSupportingDocumentRoute =
    routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode).url

  val fullSetOfFiles = UploadedFiles(
    None,
    Map.from(
      Seq
        .fill(10)(UploadedFile("filename", "www.website.com", false))
        .zipWithIndex
        .map(x => (UploadId(s"id${x._2}"), x._1))
    )
  )

  "UploadAnotherSupportingDocument Controller" - {

    val ans: UserAnswers                  = emptyUserAnswers
      .set(UploadSupportingDocumentPage, uploadedFileWithConfidentiality)
      .get
    val ansNoConfidentiality: UserAnswers =
      emptyUserAnswers.set(UploadSupportingDocumentPage, uploadedFiles).get

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val fileRows =
          SupportingDocumentsRows(uploadedFileWithConfidentiality, link)(messages(application))

        val request = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileRows, form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(ans)).build()

      running(application) {
        val request  = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)
        val fileRows =
          SupportingDocumentsRows(uploadedFileWithConfidentiality, link)(messages(application))

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileRows, form, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }
    "must redirect when missing confidentiality data" in {

      val application = applicationBuilder(userAnswers = Some(ansNoConfidentiality)).build()

      running(application) {
        val request = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(ans)).build()
      val fileRows    =
        SupportingDocumentsRows(uploadedFileWithConfidentiality, link)(messages(application))
      running(application) {
        val request =
          FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          fileRows,
          boundForm,
          NormalMode
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return a Bad Request and errors when user attempt to upload too many files" in {
      val answers: UserAnswers =
        emptyUserAnswers.set(UploadSupportingDocumentPage, fullSetOfFiles).get
      val application          = applicationBuilder(userAnswers = Some(answers)).build()

      val fileRows =
        SupportingDocumentsRows(fullSetOfFiles, link)(messages(application))

      running(application) {
        val request =
          FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val boundForm = form
          .bind(Map("value" -> "true"))
          .withError("value", "uploadAnotherSupportingDocument.error.fileCount")

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          fileRows,
          boundForm,
          NormalMode
        )(
          request,
          messages(application)
        ).toString

      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
