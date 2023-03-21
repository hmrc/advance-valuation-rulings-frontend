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
import java.time.Instant

import scala.concurrent.Future

import base.SpecBase
import models.fileupload._
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar

class UpscanCallbackDispatcherSpec extends SpecBase {

  "UpscanInitiateConnector" - {

    "handles ready callback" in new Setup {
      val callback: CallbackBody = ReadyCallbackBody(
        reference = reference,
        downloadUrl = new URL(downloadUrl),
        uploadDetails = CallbackUploadDetails(
          uploadTimestamp = Instant.parse("2018-04-24T09:30:00Z"),
          checksum = "396f101dd52e8b2ace0dcf5ed09b1d1f030e608938510ce46e7a5c7a4e775100",
          fileMimeType = mimeType,
          fileName = fileName,
          size = 45678L
        )
      )

      val dispatcher = new UpscanCallbackDispatcher(progressTracker)
      val result     = dispatcher.handleCallback(callback).eitherValue.get

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        UploadedSuccessfully(
          fileName,
          mimeType,
          fileUrl,
          Some(45678L)
        )
      )

      result mustEqual Right(())
    }

    "handles quarantine callback" in new Setup {
      val callback: CallbackBody = FailedCallbackBody(
        reference = reference,
        failureDetails = ErrorDetails(
          "QUARANTINE",
          "Invalid callback body"
        )
      )

      val dispatcher = new UpscanCallbackDispatcher(progressTracker)
      val result     = dispatcher.handleCallback(callback).eitherValue.get

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        Quarantine
      )

      result mustEqual Right(())
    }

    "handles rejected callback" in new Setup {
      val callback: CallbackBody = FailedCallbackBody(
        reference = reference,
        failureDetails = ErrorDetails(
          "REJECTED",
          "Invalid callback body"
        )
      )

      val dispatcher = new UpscanCallbackDispatcher(progressTracker)
      val result     = dispatcher.handleCallback(callback).eitherValue.get

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        Rejected
      )

      result mustEqual Right(())
    }

    "handles failed callback" in new Setup {
      val callback: CallbackBody = FailedCallbackBody(
        reference = reference,
        failureDetails = ErrorDetails(
          "FAIL",
          "Invalid callback body"
        )
      )

      val dispatcher = new UpscanCallbackDispatcher(progressTracker)
      val result     = dispatcher.handleCallback(callback).eitherValue.get

      verify(progressTracker, times(1)).registerUploadResult(
        Reference(referenceValue),
        Failed
      )

      result mustEqual Right(())
    }
  }

}

private trait Setup extends MockitoSugar {
  val referenceValue = "ref"
  val reference      = Reference(referenceValue)

  val fileName    = "test.pdf"
  val mimeType    = "application/pdf"
  val fileUrl     = "?123456"
  val downloadUrl = s"https://bucketName.s3.eu-west-2.amazonaws.com$fileUrl"

  val progressTracker = mock[UploadProgressTracker]
  when(
    progressTracker.registerUploadResult(Reference(anyString()), any())
  ) thenReturn Future.successful(())
}
