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
import models.{NormalMode, UploadedFile}
import models.upscan.UpscanInitiateResponse
import navigation.{FakeNavigator, Navigator}
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
    reset(mockFileService, mockUserAnswersService)
  }

  private lazy val redirectPath: String =
    controllers.routes.UploadSupportingDocumentsController
      .onPageLoad(NormalMode, draftId, None, None)
      .url
  private val isLetterOfAuthority       = false
  private val mockFileService           = mock[FileService]
  private val mockUserAnswersService    = mock[UserAnswersService]
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

  private val failedFile = UploadedFile.Failure(
    reference = "reference",
    failureDetails = UploadedFile.FailureDetails(
      failureReason = UploadedFile.FailureReason.Quarantine,
      failureMessage = Some("failureMessage")
    )
  )

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

      "when the key matches the file" - {

        "must show the interstitial page" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[FileService].toInstance(mockFileService)
            )
            .build()

          val view = application.injector.instanceOf[UploadSupportingDocumentsView]

          val request = FakeRequest(
            GET,
            controllers.routes.UploadSupportingDocumentsController
              .onPageLoad(models.NormalMode, draftId, None, Some("reference"))
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            draftId = draftId,
            upscanInitiateResponse = None,
            errorMessage = None
          )(messages(application), request).toString

          verify(mockFileService, times(0)).initiate(any(), any(), any())(any())
        }
      }

      "when the key does not match the file" - {

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

  "when there is a successful file" - {

    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

    "when the key matches the file" - {

      "must redirect to the next page" in {

        val onwardRoute = Call("GET", "/foo")

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FileService].toInstance(mockFileService),
            bind[Navigator].to(new FakeNavigator(onwardRoute))
          )
          .build()

        val request = FakeRequest(
          GET,
          controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(
              models.NormalMode,
              draftId,
              None,
              Some(successfulFile.reference)
            )
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verify(mockFileService, times(0)).initiate(any(), any(), any())(any())
      }
    }

    "when the key does not match the file" - {

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

    "when there is no key" - {

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

  "when there is a failed file" - {

    val userAnswers =
      userAnswersAsIndividualTrader
        .set(UploadSupportingDocumentPage, failedFile)
        .success
        .value

    "must initiate a file upload and redirect back to the page with the relevant error code" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[FileService].toInstance(mockFileService)
        )
        .build()

      when(mockFileService.initiate(any(), any(), any())(any()))
        .thenReturn(Future.successful(upscanInitiateResponse))

      val request = FakeRequest(
        GET,
        controllers.routes.UploadSupportingDocumentsController
          .onPageLoad(models.NormalMode, draftId, None, Some("key"))
          .url
      )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.UploadSupportingDocumentsController
        .onPageLoad(models.NormalMode, draftId, Some("Quarantine"), Some("key"))
        .url

      verify(mockFileService).initiate(
        eqTo(draftId),
        eqTo(redirectPath),
        eqTo(isLetterOfAuthority)
      )(any())
    }
  }

  "must redirect to the JourneyRecovery page when there are no user answers" in {

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
