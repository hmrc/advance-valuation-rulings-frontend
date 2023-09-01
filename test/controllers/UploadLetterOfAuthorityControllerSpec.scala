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
import play.api.i18n.{Messages, MessagesProvider}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import models.UploadedFile
import models.upscan.UpscanInitiateResponse
import navigation.{FakeNavigator, Navigator}
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

  private val maximumFileSizeMB: Long              = 5
  private val controller                           = controllers.routes.UploadLetterOfAuthorityController
  private lazy val redirectPath: String            =
    controllers.routes.UploadLetterOfAuthorityController
      .onPageLoad(draftId, None, None)
      .url
  private val page                                 = UploadLetterOfAuthorityPage
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

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[FileService].toInstance(mockFileService))
        .build()

      mockFileServiceInitiate()

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

  "when there is a failed file" - {

    val parameterisedCases = Table(
      ("Error code option string", "Failure Reason", "Failure message"),
      (
        "Quarantine",
        UploadedFile.FailureReason.Quarantine,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.quarantine")(messagesProvider)
      ),
      (
        "Rejected",
        UploadedFile.FailureReason.Rejected,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.rejected")(messagesProvider)
      ),
      (
        "Duplicate",
        UploadedFile.FailureReason.Duplicate,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.duplicate")(messagesProvider)
      ),
      (
        "Unknown",
        UploadedFile.FailureReason.Unknown,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.unknown")(messagesProvider)
      ),
      (
        "InvalidArgument",
        UploadedFile.FailureReason.InvalidArgument,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.invalidargument")(messagesProvider)
      ),
      (
        "EntityTooSmall",
        UploadedFile.FailureReason.EntityTooSmall,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.entitytoosmall")(messagesProvider)
      ),
      (
        "EntityTooLarge",
        UploadedFile.FailureReason.EntityTooLarge,
        (messagesProvider: MessagesProvider) =>
          Messages.apply(s"fileUpload.error.entitytoolarge", maximumFileSizeMB)(
            messagesProvider
          )
      )
    )

    "Parameterised: must initiate a file upload and redirect back to the page with the relevant error code" in {
      forAll(parameterisedCases) {
        (
          errCode: String,
          failureReason: UploadedFile.FailureReason,
          _: MessagesProvider => String
        ) =>
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

    "Parameterised: A redirect with an error code renders the error message" in {
      forAll(parameterisedCases) {
        (
          errCode: String,
          _: UploadedFile.FailureReason,
          errMessage: MessagesProvider => String
        ) =>
          val initiatedFile = UploadedFile.Initiated(
            reference = "reference"
          )

          val userAnswers = userAnswersAsIndividualTrader.set(page, initiatedFile).success.value

          val application = applicationBuilder(Some(userAnswers))
            .overrides(bind[FileService].toInstance(mockFileService))
            .build()

          mockFileServiceInitiate()

          val request = FakeRequest(
            GET,
            controller
              .onPageLoad(draftId, Some(errCode), None)
              .url
          )

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual injectView(application)(
            draftId = draftId,
            upscanInitiateResponse = Some(upscanInitiateResponse),
            errorMessage = Some(errMessage(messages(application)))
          )(messages(application), request).toString
      }
    }
  }
}
