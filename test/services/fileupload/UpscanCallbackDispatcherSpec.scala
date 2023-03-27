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
import java.net.URL
import java.time.{Clock, Instant, ZoneOffset}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.{ObjectSummaryWithMd5, Path}
import uk.gov.hmrc.objectstore.client.Md5Hash
import uk.gov.hmrc.objectstore.client.play._

import base.SpecBase
import config.FrontendAppConfig
import models.fileupload._
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar

class UpscanCallbackDispatcherSpec extends SpecBase {

  "UpscanCallbackDispatcherSpec" - {

    "handles ready callback" in new Setup {
      val callback: CallbackBody = ReadyCallbackBody(
        reference = reference,
        downloadUrl = new URL(downloadUrl),
        uploadDetails = CallbackUploadDetails(
          uploadTimestamp = Instant.parse("2018-04-24T09:30:00Z"),
          checksum = checksum,
          fileMimeType = mimeType,
          fileName = fileName,
          size = fileSize
        )
      )
      when(
        progressTracker.registerUploadResult(Reference(anyString()), any())
      ) thenReturn Future.successful(())

      implicit val hc = HeaderCarrier()
      val dispatcher  =
        new UpscanCallbackDispatcher(progressTracker, objectStoreClient, frontendAppConfig)
      val result      = dispatcher.handleCallback(callback).futureValue

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        UploadedSuccessfully(
          fileName,
          mimeType,
          "rulings/ref/test.pdf",
          checksum,
          Some(fileSize)
        )
      )

      result mustEqual ()
    }

    "handles quarantine callback" in new Setup {
      val callback: CallbackBody = FailedCallbackBody(
        reference = reference,
        failureDetails = ErrorDetails(
          "QUARANTINE",
          "Invalid callback body"
        )
      )

      implicit val hc = HeaderCarrier()
      val dispatcher  =
        new UpscanCallbackDispatcher(progressTracker, objectStoreClient, frontendAppConfig)
      val result      = dispatcher.handleCallback(callback).futureValue

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        Quarantine
      )

      result mustEqual ()
    }

    "handles rejected callback" in new Setup {
      val callback: CallbackBody = FailedCallbackBody(
        reference = reference,
        failureDetails = ErrorDetails(
          "REJECTED",
          "Invalid callback body"
        )
      )
      implicit val hc            = HeaderCarrier()

      val dispatcher =
        new UpscanCallbackDispatcher(progressTracker, objectStoreClient, frontendAppConfig)
      val result     = dispatcher.handleCallback(callback).futureValue

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        Rejected
      )

      result mustEqual ()
    }

    "handles failed callback" in new Setup {
      val callback: CallbackBody = FailedCallbackBody(
        reference = reference,
        failureDetails = ErrorDetails(
          "FAIL",
          "Invalid callback body"
        )
      )
      implicit val hc            = HeaderCarrier()

      val dispatcher =
        new UpscanCallbackDispatcher(progressTracker, objectStoreClient, frontendAppConfig)
      val result     = dispatcher.handleCallback(callback).futureValue

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        Failed
      )

      result mustEqual ()
    }
  }

}

private trait Setup extends MockitoSugar {
  val referenceValue = "ref"
  val reference      = Reference(referenceValue)
  val appName        = "advance-valuation-rulings-frontend"
  val fileName       = "test.pdf"
  val mimeType       = "application/pdf"
  val fileUrl        = "?123456"
  val fileSize       = 12345L
  val downloadUrl    = s"https://bucketName.s3.eu-west-2.amazonaws.com$fileUrl"

  val objectLocation = s"$appName/rulings/$referenceValue/$fileName"
  val checksum       = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100"

  val progressTracker   = mock[UploadProgressTracker]
  val objectStoreClient = mock[PlayObjectStoreClient]
  val frontendAppConfig = mock[FrontendAppConfig]

  val lastUpdated = Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC))

  val objectSummary = ObjectSummaryWithMd5(
    location = Path.File(objectLocation),
    contentLength = fileSize,
    contentMd5 = Md5Hash("contentMd5"),
    lastModified = lastUpdated
  )

  when(frontendAppConfig.objectStoreOwner)
    .thenReturn(appName)
  when(
    progressTracker.registerUploadResult(Reference(anyString()), any())
  ) thenReturn Future.successful(())
  when(
    objectStoreClient.uploadFromUrl(
      any(),
      any(),
      any(),
      any[Option[String]](),
      any(),
      eqTo(appName)
    )(any())
  )
    .thenReturn(Future.successful(objectSummary))
}
