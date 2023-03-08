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

package services

import javax.inject.Inject

import scala.concurrent.Future

import uk.gov.hmrc.http.HeaderCarrier

import models.fileupload._
import services.fileupload.{FileUploadResult, FileUploadService}

class FakeFileUploadService @Inject() extends FileUploadService {
  import FakeFileUploadService._
  def initiateUpload(mode: models.Mode)(implicit
    hc: HeaderCarrier
  ): Future[FileUploadResult] = Future.successful(NewUploadResult)

  def initiateWithExisting(fileUploadIds: FileUploadIds, mode: models.Mode)(implicit
    hc: HeaderCarrier
  ): Future[FileUploadResult] = Future.successful(ExistingUploadResult)

  def getUploadStatus(uploadId: UploadId): Future[Option[UploadStatus]] = uploadId match {
    case NewUploadId      => Future.successful(Some(NotStarted))
    case ExistingUploadId => Future.successful(Some(InProgress))
    case _                => Future.successful(None)
  }

}

object FakeFileUploadService {
  private val meta    = Map(
    "Content-Type"                       -> "application/xml",
    "x-amz-meta-callback-url"            -> "https://bucketName.s3.eu-west-2.amazonaws.com",
    "x-amz-meta-success-action-redirect" -> "https://bucketName.s3.eu-west-2.amazonaws.com",
    "x-amz-meta-error-action-redirect"   -> "https://bucketName.s3.eu-west-2.amazonaws.com"
  )
  val NewUploadId     = UploadId("id")
  val NewUploadResult = FileUploadResult(
    UpscanInitiateResponse(
      fileReference = UpscanFileReference("ref"),
      postTarget = "https://bucketName.s3.eu-west-2.amazonaws.com",
      meta
    ),
    NotStarted,
    NewUploadId
  )

  val ExistingUploadId     = UploadId("existing")
  val ExistingUploadResult = FileUploadResult(
    UpscanInitiateResponse(
      fileReference = UpscanFileReference("ref"),
      postTarget = "https://bucketName.s3.eu-west-2.amazonaws.com",
      meta
    ),
    InProgress,
    ExistingUploadId
  )
}
