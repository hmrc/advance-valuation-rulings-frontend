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
import models.{DraftId, Index, NormalMode, UploadedFile, UserAnswers}
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
import pages.UploadSupportingDocumentPage
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
      when(mockUserAnswersService.get(any())).thenReturn(Future.successful(Some(userAnswers)))
      when(mockUserAnswersService.set(any())).thenReturn(Future.successful(true))

      service.initiate(DraftId(0), NormalMode, Index(0))(hc).futureValue mustEqual response

      verify(mockUpscanConnector).initiate(eqTo(expectedRequest))(eqTo(hc))
      verify(mockUserAnswersService).set(userAnswersCaptor.capture())

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
      when(mockUserAnswersService.get(any())).thenReturn(Future.successful(None))

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
          checksum = "checksum"
        )
      )

      "must transfer the file to object-store and update the user answers with the status of the file" in {

        val updatedFile = uploadedFile.copy(downloadUrl = "object-store/foobar")

        val expectedAnswers = userAnswers
          .set(UploadSupportingDocumentPage(Index(0)), updatedFile)
          .success
          .value

        when(mockUserAnswersService.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockObjectStoreClient.uploadFromUrl(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(objectSummary))
        when(mockUserAnswersService.set(any())).thenReturn(Future.successful(true))

        service.update(DraftId(0), Index(0), uploadedFile).futureValue

        verify(mockUserAnswersService).get(eqTo(DraftId(0)))
        verify(mockObjectStoreClient).uploadFromUrl(any(), any(), any(), any(), any(), any())(any())
        verify(mockUserAnswersService).set(eqTo(expectedAnswers))
      }

      "must fail if no user answers can be found" in {

        when(mockUserAnswersService.get(any())).thenReturn(Future.successful(None))
        when(mockObjectStoreClient.uploadFromUrl(any(), any(), any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(objectSummary))

        service.update(DraftId(0), Index(0), uploadedFile).failed.futureValue

        verify(mockObjectStoreClient).uploadFromUrl(any(), any(), any(), any(), any(), any())(any())
        verify(mockUserAnswersService, never).set(any())
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

        when(mockUserAnswersService.get(any()))
          .thenReturn(Future.successful(Some(userAnswers)))
        when(mockUserAnswersService.set(any())).thenReturn(Future.successful(true))

        service.update(DraftId(0), Index(0), uploadedFile).futureValue

        verify(mockUserAnswersService).get(eqTo(DraftId(0)))
        verify(mockObjectStoreClient, never).uploadFromUrl(
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any())
        verify(mockUserAnswersService).set(eqTo(expectedAnswers))
      }
    }
  }
}
