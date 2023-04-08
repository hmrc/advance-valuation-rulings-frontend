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

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.{ObjectSummaryWithMd5, Path}
import uk.gov.hmrc.objectstore.client.play._

import config.FrontendAppConfig
import models.fileupload._

sealed trait FileStatus
case class Ready(callback: ReadyCallbackBody, location: String) extends FileStatus
case class NotReady(callback: FailedCallbackBody) extends FileStatus

@Singleton()
class UpscanCallbackDispatcher @Inject() (
  progressTracker: UploadProgressTracker,
  objectStoreClient: PlayObjectStoreClient,
  config: FrontendAppConfig
) {

  private lazy val logger                                     = Logger(this.getClass)
  private def directory(reference: Reference): Path.Directory =
    Path.Directory(s"rulings/${reference.value}")

  def handleCallback(
    callback: CallbackBody
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Unit] =
    for {
      uploaded <- sendFile(callback)
      status    = makeStatus(uploaded)
      result   <- progressTracker.registerUploadResult(callback.reference, status)
    } yield result

  private def makeStatus(fileStatus: FileStatus): UploadStatus =
    fileStatus match {
      case Ready(callBack, location) =>
        logger.info(
          s"Successful uploaded notification for file: ${callBack.uploadDetails.fileName}"
        )
        UploadedSuccessfully(
          callBack.uploadDetails.fileName,
          callBack.uploadDetails.fileMimeType,
          location,
          callBack.uploadDetails.checksum,
          Some(callBack.uploadDetails.size)
        )
      case NotReady(f)               =>
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
  ): Future[FileStatus] =
    callback match {
      case body: ReadyCallbackBody =>
        val filePath = directory(body.reference)
        logger.info(s"Uploading to Object Store with reference: ${body.reference.value}")
        objectStoreClient
          .uploadFromUrl(
            from = body.downloadUrl,
            to = Path.File(filePath, body.uploadDetails.fileName),
            contentType = Some(body.uploadDetails.fileMimeType),
            contentMd5 = None
          )
          .map {
            (_: ObjectSummaryWithMd5) =>
              logger.warn(s"File uploaded to Object Store with reference: ${body.reference.value}")
              Ready(body, Path.File(filePath, body.uploadDetails.fileName).asUri)
          }
          .recover {
            case e: Throwable =>
              logger.error(
                s"Failed to upload to Object Store with reference: ${body.reference.value}",
                e
              )
              NotReady(
                FailedCallbackBody(
                  body.reference,
                  ErrorDetails("Error uploading to object store", e.getMessage)
                )
              )
          }
      case f: FailedCallbackBody   =>
        Future.successful(NotReady(f))
    }
}
