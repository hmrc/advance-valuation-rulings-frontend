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

import javax.inject.Inject

import scala.concurrent.Future

import play.api.Logger

import connectors.BackendConnector
import models.fileupload.{CallbackBody, Failed, FailedCallbackBody, Quarantine, ReadyCallbackBody, Rejected, UploadedSuccessfully}

class UpscanCallbackDispatcher @Inject() (
  sessionStorage: UploadProgressTracker,
  backendConnector: BackendConnector
) {

  private val logger = Logger(this.getClass)

  def handleCallback(callback: CallbackBody): Future[Unit] = {

    val uploadStatus = callback match {
      case s: ReadyCallbackBody  =>
        logger.info(s"Successful uploaded notification for file: ${s.uploadDetails.fileName}")
        UploadedSuccessfully(
          s.uploadDetails.fileName,
          s.uploadDetails.fileMimeType,
          s.downloadUrl.getFile,
          Some(s.uploadDetails.size)
        )
      case f: FailedCallbackBody =>
        val upscanFailureDetails = f.failureDetails.failureReason
        logger.warn(
          s"File upload failed notification received from upscan: $upscanFailureDetails with reference: ${f.reference.value}"
        )
        upscanFailureDetails match {
          case "QUARANTINE" => Quarantine
          case "REJECTED"   => Rejected
          case _            => Failed
        }
    }

    sessionStorage.registerUploadResult(callback.reference, uploadStatus)
  }

}
