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
import javax.inject.{Inject, Singleton}
import javax.security.auth.callback.Callback

import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client._
import uk.gov.hmrc.objectstore.client.{ObjectSummaryWithMd5, Path}
import uk.gov.hmrc.objectstore.client.play._

import com.google.inject.ImplementedBy
import models.fileupload._

@Singleton()
class UpscanCallbackDispatcher @Inject() (
  progressTracker: UploadProgressTracker,
  objectStoreClient: PlayObjectStoreClient
) {

  private lazy val logger                                     = Logger(this.getClass)
  private def directory(reference: Reference): Path.Directory =
    Path.Directory(s"rulings/${reference.value}")

  def handleCallback(
    callback: CallbackBody
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Unit] = {
    // val sendFileFuture = sendFile(callback)
    // for {
    //   // callback <- sendFileFuture
    //   // status    = makeStatus(callback)
    //   res <- progressTracker.registerUploadResult(callback.reference, makeStatus(callback))
    // } yield res
    val newCallback: Future[CallbackBody] = sendFile(callback)

    val status = newCallback.map(makeStatus(_))
    status.flatMap(status => progressTracker.registerUploadResult(callback.reference, status))
  }

  private def makeStatus(callback: CallbackBody): UploadStatus =
    callback match {
      case body: ReadyCallbackBody =>
        logger.info(s"Successful uploaded notification for file: ${body.uploadDetails.fileName}")

        UploadedSuccessfully(
          body.uploadDetails.fileName,
          body.uploadDetails.fileMimeType,
          body.downloadUrl.getFile,
          Some(body.uploadDetails.size)
        )
      case f: FailedCallbackBody   =>
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

  private def sendFile(callback: CallbackBody)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[CallbackBody] =
    callback match {
      case body: ReadyCallbackBody =>
        val filePath = directory(body.reference)
        objectStoreClient
          .uploadFromUrl(
            from = body.downloadUrl,
            to = Path.File(filePath, body.uploadDetails.fileName),
            contentType = Some(body.uploadDetails.fileMimeType),
            contentMd5 = None,
            owner = "advance-valuation-ruling-frontend"
          )
          .map {
            (summary: ObjectSummaryWithMd5) =>
              logger.debug(s"Valuation application stored with reference: ${body.reference.value}")
              body.copy(downloadUrl = new URL(summary.location.asUri))
          }
      case _: FailedCallbackBody   =>
        Future.successful(callback)
    }
  /* Error handling can be left to Bootstrap or customised */
  // .recover {
  //   case UpstreamErrorResponse(message, statusCode, _, _) =>
  //     logger.error(s"Upstream error with status code '$statusCode' and message: $message")
  //     InternalServerError("Upstream error encountered")
  //   case e: Exception                                     =>
  //     logger.error(s"An error was encountered saving the document.", e)
  //     InternalServerError("Error saving the document")
  // }

}
