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

import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import models.{NormalMode, UploadedFile}
import models.upscan.UpscanInitiateResponse
import navigation.{FakeNavigator, Navigator}
import org.apache.pdfbox.pdmodel.PDDocument
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{verify, when}
import org.mockito.MockitoSugar.{reset, times}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages._
import services.UserAnswersService
import services.fileupload.FileService
import views.html.UploadLetterOfAuthorityView

class UploadLetterOfAuthorityControllerSpec
    extends SpecBase
    with MockitoSugar
    with BeforeAndAfterEach
    with TableDrivenPropertyChecks {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileService, mockUserAnswersService)
  }

  private val controller                           = controllers.routes.UploadLetterOfAuthorityController
  private val redirectPath                         =
    "/advance-valuation-ruling" + controller.onPageLoad(draftId, None, None).url
  private val page                                 = UploadLetterOfAuthorityPage
  private val unknownError                         = "uploadLetterOfAuthority.error.unknown"
  private def injectView(application: Application) =
    application.injector.instanceOf[UploadLetterOfAuthorityView]

  private val mockFileService        = mock[FileService]
  private val mockUserAnswersService = mock[UserAnswersService]

  private def mockFileServiceInitiate(): Unit =
    when(
      mockFileService.initiate(eqTo(draftId), eqTo(redirectPath), eqTo(true))(
        any()
      )
    ).thenReturn(Future.successful(upscanInitiateResponse))

  private def verifyFileServiceInitiate(): Unit =
    verify(mockFileService).initiate(
      eqTo(draftId),
      eqTo(redirectPath),
      eqTo(true)
    )(any())

  private def verifyFileServiceInitiateZeroTimes(): Unit =
    verify(mockFileService, times(0))
      .initiate(eqTo(draftId), eqTo(redirectPath), eqTo(true))(any())

  private val upscanInitiateResponse = UpscanInitiateResponse(
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

  "when there is no existing file" - {

    "must initiate a file upload and display the page" in {

      mockFileServiceInitiate()

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[FileService].toInstance(mockFileService))
        .build()

      val request = FakeRequest(
        GET,
        controller.onPageLoad(draftId, None, None).url
      )

      val result = route(application, request).value

      status(result) mustEqual OK
      contentAsString(result) mustEqual injectView(application)(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = None
      )(messages(application), request).toString

      verifyFileServiceInitiate()
    }
  }

  "when there is an initiated file" - {

    val userAnswers = userAnswersAsIndividualTrader.set(page, initiatedFile).success.value

    "when there is an error code" - {
      // TODO: Remove, since this should be included in the parameterised test below.
      "must initiate a file upload and display the page with errors" in {

        mockFileServiceInitiate()

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FileService].toInstance(mockFileService))
          .build()

        val request = FakeRequest(
          GET,
          controller.onPageLoad(draftId, Some("errorCode"), None).url
        )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual injectView(application)(
          draftId = draftId,
          upscanInitiateResponse = Some(upscanInitiateResponse),
          errorMessage = Some(messages(application)(unknownError))
        )(messages(application), request).toString

        verifyFileServiceInitiate()
      }

    }

    "when there is no error code" - {

      "when the key matches the file" - {

        "must show the interstitial page" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[FileService].toInstance(mockFileService))
            .build()

          val request = FakeRequest(
            GET,
            controller.onPageLoad(draftId, None, Some("reference")).url
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual injectView(application)(
            draftId = draftId,
            upscanInitiateResponse = None,
            errorMessage = None
          )(messages(application), request).toString

          verifyFileServiceInitiateZeroTimes()
        }
      }

      "when the key does not match the file" - {

        "must initiate a file upload and display the page" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[FileService].toInstance(mockFileService))
            .build()

          mockFileServiceInitiate()

          val request = FakeRequest(
            GET,
            controller.onPageLoad(draftId, None, Some("otherReference")).url
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual injectView(application)(
            draftId = draftId,
            upscanInitiateResponse = Some(upscanInitiateResponse),
            errorMessage = None
          )(messages(application), request).toString

          verifyFileServiceInitiate()
        }
      }

      "when the key does not exist" - {

        "must initiate a file upload and display the page" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[FileService].toInstance(mockFileService))
            .build()

          mockFileServiceInitiate()

          val request = FakeRequest(
            GET,
            controller
              .onPageLoad(draftId, None, None)
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual injectView(application)(
            draftId = draftId,
            upscanInitiateResponse = Some(upscanInitiateResponse),
            errorMessage = None
          )(messages(application), request).toString

          verifyFileServiceInitiate()
        }
      }
    }
  }

  "when there is a successful file" - {

    val userAnswers = userAnswersAsIndividualTrader.set(page, successfulFile).success.value

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
          controller.onPageLoad(draftId, None, Some(successfulFile.reference)).url
        )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        verifyFileServiceInitiateZeroTimes()
      }
    }

    "when the key does not match the file" - {

      "must initiate a file upload and display the page" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FileService].toInstance(mockFileService))
          .build()

        mockFileServiceInitiate()

        val request = FakeRequest(
          GET,
          controller.onPageLoad(draftId, None, Some("otherReference")).url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual injectView(application)(
          draftId = draftId,
          upscanInitiateResponse = Some(upscanInitiateResponse),
          errorMessage = None
        )(messages(application), request).toString

        verifyFileServiceInitiate()
      }
    }

    "when there is no key" - {

      "must initiate a file upload and display the page" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[FileService].toInstance(mockFileService))
          .build()

        mockFileServiceInitiate()

        val request = FakeRequest(
          GET,
          controller
            .onPageLoad(draftId, None, None)
            .url
        )

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual injectView(application)(
          draftId = draftId,
          upscanInitiateResponse = Some(upscanInitiateResponse),
          errorMessage = None
        )(messages(application), request).toString

        verifyFileServiceInitiate()
      }
    }
  }

  "must redirect to the JourneyRecovery page when there are no user answers" in {

    val application = applicationBuilder(userAnswers = None)
      .overrides(bind[FileService].toInstance(mockFileService))
      .build()

    val request = FakeRequest(
      GET,
      controller
        .onPageLoad(draftId, None, None)
        .url
    )

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

    verifyFileServiceInitiateZeroTimes()
  }

  // New tests

  "when there is a failed file" - {

    val parameterisedCases = Table(
      ("Error code option string", "Failure Reason"),
      ("Quarantine", UploadedFile.FailureReason.Quarantine),
      ("Rejected", UploadedFile.FailureReason.Rejected),
      ("Duplicate", UploadedFile.FailureReason.Duplicate),
      ("Unknown", UploadedFile.FailureReason.Unknown)
    )

    "Parameterised: must initiate a file upload and redirect back to the page with the relevant error code" in {
      forAll(parameterisedCases) {
        (errCode: String, failureReason: UploadedFile.FailureReason) =>
          val failedFile = UploadedFile.Failure(
            reference = "reference",
            failureDetails = UploadedFile.FailureDetails(failureReason, Some("failureMessage"))
          )

          val userAnswers = userAnswersAsIndividualTrader.set(page, failedFile).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[FileService].toInstance(mockFileService))
            .build()

          mockFileServiceInitiate()

          val request = FakeRequest(
            GET,
            controller.onPageLoad(draftId, None, Some("key")).url
          )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controller
            .onPageLoad(draftId, Some(errCode), Some("key"))
            .url
      }
    }

    "A redirect with an error code renders the error message" in {

      mockFileServiceInitiate()
      val initiatedFile = UploadedFile.Initiated(
        reference = "reference"
      )

      val userAnswers = userAnswersAsIndividualTrader.set(page, initiatedFile).success.value

      val application = applicationBuilder(Some(userAnswers))
        .overrides(bind[FileService].toInstance(mockFileService))
        .build()
      val request     = FakeRequest(
        GET,
        controller
          .onPageLoad(draftId, Some("error.code"), None)
          .url
      )

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual injectView(application)(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = Some("uploadLetterOfAuthority.error.unknown")
      )(messages(application), request).toString
    }
  }
}
