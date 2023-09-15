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
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import controllers.common.FileUploadHelper
import models.{NormalMode, UploadedFile}
import models.upscan.UpscanInitiateResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.{reset, times}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages._
import services.UserAnswersService
import services.fileupload.FileService
import views.html.UploadSupportingDocumentsView

class UploadSupportingDocumentsControllerSpec
    extends SpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileService, mockFileUploadHelper)
  }

  private lazy val redirectPath: String =
    controllers.routes.UploadSupportingDocumentsController
      .onPageLoad(NormalMode, draftId, None, None)
      .url
  private val isLetterOfAuthority       = false

  private val mockFileService           = mock[FileService]
  private val mockFileUploadHelper      = mock[FileUploadHelper]
  private val upscanInitiateResponse    = UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "href",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )

  private val initiatedFile =
    UploadedFile.Initiated("reference")

  private val successfulFile: UploadedFile.Success = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.EPOCH,
      checksum = "checksum",
      size = 1337
    )
  )

  "When the page is loaded it must display the expected content" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FileService].toInstance(mockFileService),
        bind[FileUploadHelper].toInstance(mockFileUploadHelper)
      )
      .build()

    val successTextForHelper = "test upload supporting document"
    when(
      mockFileUploadHelper.onPageLoadWithFileStatus(
        eqTo(NormalMode),
        eqTo(draftId),
        eqTo(None),
        eqTo(None),
        eqTo(Some(successfulFile)),
        eqTo(isLetterOfAuthority)
      )(any(), any())
    )
      .thenReturn(Future.successful(play.api.mvc.Results.Ok(successTextForHelper)))

    val request = FakeRequest(
      GET,
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(NormalMode, draftId, None, None)
        .url
    )

    val result = route(application, request).value
    contentAsString(result) mustEqual successTextForHelper
  }

  "when there is no existing file" - {

    "must initiate a file upload and display the page" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[FileService].toInstance(mockFileService)
        )
        .build()

      val view = application.injector.instanceOf[UploadSupportingDocumentsView]

      when(mockFileService.initiate(any(), any(), any())(any()))
        .thenReturn(Future.successful(upscanInitiateResponse))

      val request = FakeRequest(
        GET,
        controllers.routes.UploadSupportingDocumentsController
          .onPageLoad(models.NormalMode, draftId, None, None)
          .url
      )

      val result = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = None
      )(messages(application), request).toString

      verify(mockFileService).initiate(
        eqTo(draftId),
        eqTo(redirectPath),
        eqTo(isLetterOfAuthority)
      )(any())
    }
  }

  "when there is an initiated file" - {

    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, initiatedFile)
      .success
      .value

    "when there is an error code" - {

      "must initiate a file upload and display the page with errors" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileService].toInstance(mockFileService)
          )
          .build()

        val view = application.injector.instanceOf[UploadSupportingDocumentsView]

        when(mockFileService.initiate(any(), any(), any())(any()))
          .thenReturn(Future.successful(upscanInitiateResponse))

        val request = FakeRequest(
          GET,
          controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(models.NormalMode, draftId, Some("errorCode"), None)
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(
          draftId = draftId,
          upscanInitiateResponse = Some(upscanInitiateResponse),
          errorMessage = Some(messages(application)("fileUpload.error.unknown"))
        )(messages(application), request).toString

        verify(mockFileService).initiate(
          eqTo(draftId),
          eqTo(redirectPath),
          eqTo(isLetterOfAuthority)
        )(any())
      }
    }

    "when there is no error code" - {

      "when the key does not match the file" - {

        "must initiate a file upload and show page" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[FileService].toInstance(mockFileService)
            )
            .build()

          val view = application.injector.instanceOf[UploadSupportingDocumentsView]

          when(mockFileService.initiate(any(), any(), any())(any()))
            .thenReturn(Future.successful(upscanInitiateResponse))

          val request = FakeRequest(
            GET,
            controllers.routes.UploadSupportingDocumentsController
              .onPageLoad(models.NormalMode, draftId, None, Some("otherReference"))
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            draftId = draftId,
            upscanInitiateResponse = Some(upscanInitiateResponse),
            errorMessage = None
          )(messages(application), request).toString

          verify(mockFileService).initiate(
            eqTo(draftId),
            eqTo(redirectPath),
            eqTo(isLetterOfAuthority)
          )(any())
        }
      }

      "when the key does not exist" - {

        "must initiate a file upload and display the page" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[FileService].toInstance(mockFileService)
            )
            .build()

          val view = application.injector.instanceOf[UploadSupportingDocumentsView]

          when(mockFileService.initiate(any(), any(), any())(any()))
            .thenReturn(Future.successful(upscanInitiateResponse))

          val request = FakeRequest(
            GET,
            controllers.routes.UploadSupportingDocumentsController
              .onPageLoad(models.NormalMode, draftId, None, None)
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            draftId = draftId,
            upscanInitiateResponse = Some(upscanInitiateResponse),
            errorMessage = None
          )(messages(application), request).toString

          verify(mockFileService).initiate(
            eqTo(draftId),
            eqTo(redirectPath),
            eqTo(isLetterOfAuthority)
          )(any())
        }
      }
    }
  }

  "when there are no user answers" - {
    "must redirect to the JourneyRecovery page" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[FileService].toInstance(mockFileService)
        )
        .build()

      val request = FakeRequest(
        GET,
        controllers.routes.UploadSupportingDocumentsController
          .onPageLoad(models.NormalMode, draftId, None, None)
          .url
      )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

      verify(mockFileService, times(0)).initiate(any(), any(), any())(any())
    }
  }

  "when there is a successful file and the user has unexpectedly navigated back to this page" - {

    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

    "must delete file using FileUploadHelper" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileService].toInstance(mockFileService),
          bind[FileUploadHelper].toInstance(mockFileUploadHelper)
        )
        .build()

      val successTextForHelper = "test upload supporting document"
      when(
        mockFileUploadHelper.onPageLoadWithFileStatus(
          eqTo(NormalMode),
          eqTo(draftId),
          eqTo(None),
          eqTo(None),
          eqTo(Some(successfulFile)),
          eqTo(isLetterOfAuthority)
        )(any(), any())
      )
        .thenReturn(Future.successful(play.api.mvc.Results.Ok(successTextForHelper)))

      val request = FakeRequest(
        GET,
        controllers.routes.UploadSupportingDocumentsController
          .onPageLoad(NormalMode, draftId, None, None)
          .url
      )

      val result = route(application, request).value
      contentAsString(result) mustEqual successTextForHelper
    }

    "must redirect to fallback page" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileService].toInstance(mockFileService)
        )
        .build()

      val view = application.injector.instanceOf[UploadSupportingDocumentsView]

      when(mockFileService.initiate(eqTo(draftId), eqTo(redirectPath), eqTo(false))(any()))
        .thenReturn(Future.successful(upscanInitiateResponse))

      val request = FakeRequest(
        GET,
        controllers.routes.UploadSupportingDocumentsController
          .onPageLoad(NormalMode, draftId, None, None)
          .url
      )

      val result = route(application, request).value
      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = None
      )(messages(application), request).toString
    }
  }
}
