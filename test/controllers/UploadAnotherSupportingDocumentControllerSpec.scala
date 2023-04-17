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
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import base.SpecBase
import config.FrontendAppConfig
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import models.fileupload.{UploadId, UpscanFileDetails}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.UploadSupportingDocumentPage
import services.UserAnswersService
import views.html.UploadAnotherSupportingDocumentView

class UploadAnotherSupportingDocumentControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute                        = Call("GET", "/foo")
  val maximumNumberOfFiles               = 5
  val numOfDocs                          = "one"
  val upscanFileDeets: UpscanFileDetails =
    UpscanFileDetails(UploadId("id"), "name", "some.url", "txt", 1L)

  val uploadedFiles                   = UploadedFiles.initialise(upscanFileDeets)
  val uploadedFileWithConfidentiality = uploadedFiles.setConfidentiality(true)

  val formProvider = new UploadAnotherSupportingDocumentFormProvider()
  val form         = formProvider()
  val link         = new views.html.components.Link()

  lazy val uploadAnotherSupportingDocumentRoute =
    routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, draftId).url

  lazy val removeSupportingDocumentRoute =
    routes.UploadAnotherSupportingDocumentController.onDelete("id", NormalMode, draftId).url

  val fullSetOfFiles = UploadedFiles(
    None,
    Map.from(
      Seq
        .fill(maximumNumberOfFiles)(UploadedFile("filename", "www.website.com", false, "txt", 1L))
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
    lazy val application                  = applicationBuilder(userAnswers = Some(ans)).build()

    "must return OK and the correct view for a GET" in {

      running(application) {
        val fileRows =
          SupportingDocumentsRows(uploadedFileWithConfidentiality, link, NormalMode, draftId)(
            messages(application)
          )

        val request = FakeRequest(GET, uploadAnotherSupportingDocumentRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileRows, form, NormalMode, draftId)(
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
          SupportingDocumentsRows(uploadedFileWithConfidentiality, link, NormalMode, draftId)(
            messages(application)
          )

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(fileRows, form, NormalMode, draftId)(
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

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
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
        SupportingDocumentsRows(uploadedFileWithConfidentiality, link, NormalMode, draftId)(
          messages(application)
        )
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
          NormalMode,
          draftId
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when user with 10 files selects 'No'" in {

      val mockUserAnswersService = mock[UserAnswersService]
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val answers: UserAnswers =
        emptyUserAnswers.set(UploadSupportingDocumentPage, fullSetOfFiles).get
      val application          = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(
          result
        ).value mustEqual controllers.routes.CheckYourAnswersController.onPageLoad(draftId).url
      }
    }

    "must return a Bad Request and errors when user attempt to upload too many files" in {
      val answers: UserAnswers =
        emptyUserAnswers.set(UploadSupportingDocumentPage, fullSetOfFiles).get
      val application          = applicationBuilder(userAnswers = Some(answers)).build()

      val fileRows =
        SupportingDocumentsRows(fullSetOfFiles, link, NormalMode, draftId)(messages(application))

      running(application) {
        val request =
          FakeRequest(POST, uploadAnotherSupportingDocumentRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val boundForm = form
          .bind(Map("value" -> "true"))
          .withError(
            "value",
            "uploadAnotherSupportingDocument.error.fileCount",
            maximumNumberOfFiles
          )

        val view = application.injector.instanceOf[UploadAnotherSupportingDocumentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          fileRows,
          boundForm,
          NormalMode,
          draftId
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

    "must redirect on files are delete" in {

      val config                 = mock[FrontendAppConfig]
      val osClient               = mock[PlayObjectStoreClient]
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any()))
        .thenReturn(Future.successful(Done))
      when(osClient.deleteObject(any(), any())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(ans))
          .overrides(
            bind[PlayObjectStoreClient].toInstance(osClient),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[FrontendAppConfig].toInstance(config)
          )
          .build()
      running(application) {

        val request = FakeRequest(GET, removeSupportingDocumentRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockUserAnswersService, times(1)).set(any())(any())
        verify(osClient, times(1)).deleteObject(any(), any())(
          any()
        )
      }
    }

    "does not call object store if the file does not exist" in {

      val osClient               = mock[PlayObjectStoreClient]
      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any()))
        .thenReturn(Future.successful(Done))
      when(osClient.deleteObject(any(), anyString())(any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[PlayObjectStoreClient].toInstance(osClient),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()
      running(application) {

        val request = FakeRequest(GET, removeSupportingDocumentRoute)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockUserAnswersService, times(0)).set(any())(any())
        verify(osClient, times(0)).deleteObject(any(), anyString())(any())
      }
    }
  }
}
