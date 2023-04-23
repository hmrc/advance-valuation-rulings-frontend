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

package services.fileupload

import java.time.{LocalDateTime, ZoneOffset}

import scala.concurrent.Future

import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.{Md5Hash, ObjectSummaryWithMd5, Path}
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import connectors.UpscanConnector
import models.{Done, DraftId, Index, NormalMode, UploadedFile, UserAnswers}
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar._
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.{IsThisFileConfidentialPage, UploadSupportingDocumentPage}
import services.UserAnswersService

class FileServiceSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with MockitoSugar
    with BeforeAndAfterEach
    with TryValues {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUpscanConnector, mockUserAnswersService, mockObjectStoreClient)
  }

  private val mockUpscanConnector    = mock[UpscanConnector]
  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockObjectStoreClient  = mock[PlayObjectStoreClient]

  private lazy val app = GuiceApplicationBuilder()
    .configure(
      "host"                   -> "host",
      "upscan.callbackBaseUrl" -> "callbackUrl",
      "upscan.minFileSize"     -> "123b",
      "upscan.maxFileSize"     -> "321b"
    )
    .overrides(
      bind[UpscanConnector].toInstance(mockUpscanConnector),
      bind[UserAnswersService].toInstance(mockUserAnswersService),
      bind[PlayObjectStoreClient].toInstance(mockObjectStoreClient)
    )

  private lazy val service = app.injector.instanceOf[FileService]

  private val hc: HeaderCarrier = HeaderCarrier()

  private val response = UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "foobar",
      fields = Map("foo" -> "bar")
    )
  )

  "initiate" - {

    "must call the upscan connector with the expected request and update the user answers with the initiated file" in {

      val userAnswersCaptor: ArgumentCaptor[UserAnswers] =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      val userAnswers = UserAnswers("userId", DraftId(0))

      val expectedPath = controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(Index(0), NormalMode, DraftId(0), None, None)
        .url
      val expectedUrl  = s"host$expectedPath"

      val expectedRequest = UpscanInitiateRequest(
        callbackUrl = s"callbackUrl${controllers.callback.routes.UploadCallbackController
            .callback(DraftId(0), Index(0))}",
        successRedirect = expectedUrl,
        errorRedirect = expectedUrl,
        minimumFileSize = 123,
        maximumFileSize = 321
      )

      when(mockUpscanConnector.initiate(any())(any())).thenReturn(Future.successful(response))
      when(mockUserAnswersService.get(any())(any()))
        .thenReturn(Future.successful(Some(userAnswers)))
      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      service.initiate(DraftId(0), NormalMode, Index(0))(hc).futureValue mustEqual response

      verify(mockUpscanConnector).initiate(eqTo(expectedRequest))(eqTo(hc))
      verify(mockUserAnswersService).set(userAnswersCaptor.capture())(any())

      val actualAnswers = userAnswersCaptor.getValue

      actualAnswers.get(UploadSupportingDocumentPage(Index(0))).value mustEqual UploadedFile
        .Initiated("reference")
    }

    "fail when there are no user answers for that draft" in {

      val expectedPath = controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(Index(0), NormalMode, DraftId(0), None, None)
        .url
      val expectedUrl  = s"host$expectedPath"

      val expectedRequest = UpscanInitiateRequest(
        callbackUrl = s"callbackUrl${controllers.callback.routes.UploadCallbackController
            .callback(DraftId(0), Index(0))}",
        successRedirect = expectedUrl,
        errorRedirect = expectedUrl,
        minimumFileSize = 123,
        maximumFileSize = 321
      )

      when(mockUpscanConnector.initiate(any())(any())).thenReturn(Future.successful(response))
      when(mockUserAnswersService.get(any())(any())).thenReturn(Future.successful(None))

      val exception = service.initiate(DraftId(0), NormalMode, Index(0))(hc).failed.futureValue

      verify(mockUpscanConnector).initiate(eqTo(expectedRequest))(eqTo(hc))

      exception mustBe a[FileService.NoUserAnswersFoundException]
      exception.asInstanceOf[FileService.NoUserAnswersFoundException].draftId mustEqual DraftId(0)
    }
  }

  "update" - {

    val userAnswers = UserAnswers("userId", DraftId(0))

    val instant = LocalDateTime
      .of(2023, 3, 2, 12, 30, 45)
      .toInstant(ZoneOffset.UTC)

    val objectSummary = ObjectSummaryWithMd5(
      location = Path.File("object-store/foobar"),
      contentLength = 1337,
      contentMd5 = Md5Hash("checksum"),
      lastModified = instant
    )

    "when the file is ready" - {

      val uploadedFile = UploadedFile.Success(
        reference = "reference",
        downloadUrl = "http://example.com/foobar",
        uploadDetails = UploadedFile.UploadDetails(
          fileName = "foobar",
          fileMimeType = "text/plain",
          uploadTimestamp = instant,
          checksum = "checksum",
          size = 1337
        )
      )

      "must transfer the file to object-store and update the user answers with the status of the file" in {

        val updatedFile = uploadedFile.copy(downloadUrl = "drafts/DRAFT000000000/foobar")

        val expectedAnswers = userAnswers
          .set(UploadSupportingDocumentPage(Index(0)), updatedFile)
          .success
          .value

        when(mockUserAnswersService.getInternal(any())(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockObjectStoreClient.uploadFromUrl(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(objectSummary))
        when(mockUserAnswersService.setInternal(any())(any())).thenReturn(Future.successful(Done))

        service.update(DraftId(0), Index(0), uploadedFile).futureValue

        verify(mockUserAnswersService).getInternal(eqTo(DraftId(0)))(any())
        verify(mockObjectStoreClient).uploadFromUrl(any(), any(), any(), any(), any(), any())(any())
        verify(mockUserAnswersService).setInternal(eqTo(expectedAnswers))(any())
      }

      "must fail if no user answers can be found" in {

        when(mockUserAnswersService.getInternal(any())(any())).thenReturn(Future.successful(None))
        when(mockObjectStoreClient.uploadFromUrl(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(objectSummary))

        service.update(DraftId(0), Index(0), uploadedFile).failed.futureValue

        verify(mockObjectStoreClient, never).uploadFromUrl(
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any())
        verify(mockUserAnswersService, never).setInternal(any())(any())
      }
    }

    "when the file has failed" - {

      val uploadedFile = UploadedFile.Failure(
        reference = "reference",
        failureDetails = UploadedFile.FailureDetails(
          failureReason = UploadedFile.FailureReason.Quarantine,
          failureMessage = Some("failureMessage")
        )
      )

      "must update the user answers with the status of the file" in {

        val expectedAnswers = userAnswers
          .set(UploadSupportingDocumentPage(Index(0)), uploadedFile)
          .success
          .value

        when(mockUserAnswersService.getInternal(any())(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockUserAnswersService.setInternal(any())(any())).thenReturn(Future.successful(Done))

        service.update(DraftId(0), Index(0), uploadedFile).futureValue

        verify(mockUserAnswersService).getInternal(eqTo(DraftId(0)))(any())
        verify(mockObjectStoreClient, never).uploadFromUrl(
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any())
        verify(mockUserAnswersService).setInternal(eqTo(expectedAnswers))(any())
      }
    }

    "when the file name is the same as an existing file for this draft" - {

      "must not transfer the file to object-store and update the user answers with a failed status" in {

        val file1 = UploadedFile.Success(
          reference = "reference",
          downloadUrl = "http://example.com/foobar",
          uploadDetails = UploadedFile.UploadDetails(
            fileName = "foobar",
            fileMimeType = "text/plain",
            uploadTimestamp = instant,
            checksum = "checksum",
            size = 1337
          )
        )

        val file2 = file1.copy(reference = "reference2")

        val file3 = UploadedFile.Failure(
          reference = "reference2",
          failureDetails = UploadedFile.FailureDetails(
            failureReason = UploadedFile.FailureReason.Duplicate,
            failureMessage = None
          )
        )

        val userAnswers = UserAnswers("userId", DraftId(0))
          .set(UploadSupportingDocumentPage(Index(0)), file1)
          .success
          .value
          .set(IsThisFileConfidentialPage(Index(0)), true)
          .success
          .value

        val expectedAnswers = userAnswers
          .set(UploadSupportingDocumentPage(Index(1)), file3)
          .success
          .value

        when(mockUserAnswersService.getInternal(any())(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockUserAnswersService.setInternal(any())(any())).thenReturn(Future.successful(Done))

        service.update(DraftId(0), Index(1), file2).futureValue

        verify(mockUserAnswersService).getInternal(eqTo(DraftId(0)))(any())
        verify(mockObjectStoreClient, never).uploadFromUrl(
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any())
        verify(mockUserAnswersService).setInternal(eqTo(expectedAnswers))(any())
      }

      "must allow an upload of a file with the same name if the index matches" in {

        val file1 = UploadedFile.Success(
          reference = "reference",
          downloadUrl = "http://example.com/foobar",
          uploadDetails = UploadedFile.UploadDetails(
            fileName = "foobar",
            fileMimeType = "text/plain",
            uploadTimestamp = instant,
            checksum = "checksum",
            size = 1337
          )
        )

        val file2 = file1.copy(
          reference = "reference2"
        )

        val file3 = file2.copy(
          downloadUrl = "drafts/DRAFT000000000/foobar"
        )

        val userAnswers = UserAnswers("userId", DraftId(0))
          .set(UploadSupportingDocumentPage(Index(0)), file1)
          .success
          .value

        val expectedAnswers = userAnswers
          .set(UploadSupportingDocumentPage(Index(0)), file3)
          .success
          .value

        when(mockUserAnswersService.getInternal(any())(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockObjectStoreClient.uploadFromUrl(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(objectSummary))
        when(mockUserAnswersService.setInternal(any())(any())).thenReturn(Future.successful(Done))

        service.update(DraftId(0), Index(0), file2).futureValue

        verify(mockUserAnswersService).getInternal(eqTo(DraftId(0)))(any())
        verify(mockObjectStoreClient).uploadFromUrl(any(), any(), any(), any(), any(), any())(any())
        verify(mockUserAnswersService).setInternal(eqTo(expectedAnswers))(any())
      }
    }
  }
}
