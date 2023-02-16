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

import cats.syntax.all._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import uk.gov.hmrc.http.HeaderCarrier

import config.FrontendAppConfig
import connectors.UpscanInitiateConnector
import models.fileupload._
import services.fileupload.UploadProgressTracker

case class FileUploadResult(
  upscanResponse: UpscanInitiateResponse,
  uploadStatus: UploadStatus,
  redirectFileId: UploadId
)

trait FileUploadService {
  def initiateUpload()(implicit
    hc: HeaderCarrier
  ): Future[FileUploadResult]

  def initiateWithExisting(fileUploadIds: FileUploadIds)(implicit
    hc: HeaderCarrier
  ): Future[FileUploadResult]

  def getUploadStatus(uploadId: UploadId): Future[Option[UploadStatus]]
}

class UpscanFileUploadService @Inject() (
  upscanInitiateConnector: UpscanInitiateConnector,
  uploadProgressTracker: UploadProgressTracker,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FileUploadService {

  def initiateUpload()(implicit
    hc: HeaderCarrier
  ): Future[FileUploadResult] = {
    val fileUploadIds       = FileUploadIds.generateNewFileUploadId
    val redirectUrlFileId   = fileUploadIds.redirectUrlFileId
    val requestUploadFileId = fileUploadIds.nextUploadFileId

    val baseUrl            = appConfig.host
    val redirectUrl        = controllers.fileupload.routes.UploadSupportingDocumentsController
      .onPageLoad(None, None, Some(redirectUrlFileId))
      .url
    val errorRedirectUrl   = s"$baseUrl/advance-valuation-ruling/uploadSupportingDocuments".some
    val successRedirectUrl = s"${baseUrl}$redirectUrl".some

    for {
      response <- upscanInitiateConnector.initiateV2(successRedirectUrl, errorRedirectUrl)
      _        <- uploadProgressTracker.requestUpload(
                    requestUploadFileId,
                    Reference(response.fileReference.reference)
                  )
    } yield FileUploadResult(response, NotStarted, redirectUrlFileId)
  }

  def initiateWithExisting(fileUploadIds: FileUploadIds)(implicit
    hc: HeaderCarrier
  ) = {
    val redirectUrlFileId   = fileUploadIds.redirectUrlFileId
    val requestUploadFileId = fileUploadIds.nextUploadFileId

    val baseUrl     = appConfig.host
    val redirectUrl = controllers.fileupload.routes.UploadSupportingDocumentsController
      .onPageLoad(None, None, Some(redirectUrlFileId))
      .url

    val errorRedirectUrl   = s"$baseUrl/advance-valuation-ruling/uploadSupportingDocuments".some
    val successRedirectUrl = s"${baseUrl}$redirectUrl".some

    for {
      response <- upscanInitiateConnector.initiateV2(successRedirectUrl, errorRedirectUrl)
      _        <- uploadProgressTracker.requestUpload(
                    requestUploadFileId,
                    Reference(response.fileReference.reference)
                  )
      status   <- getUploadStatus(redirectUrlFileId)
    } yield FileUploadResult(
      response,
      status.getOrElse(NotStarted),
      redirectUrlFileId
    )

  }

  def getUploadStatus(uploadId: UploadId): Future[Option[UploadStatus]] =
    uploadProgressTracker.getUploadResult(uploadId)

}
