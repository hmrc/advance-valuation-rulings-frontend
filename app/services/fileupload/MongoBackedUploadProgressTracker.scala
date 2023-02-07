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

import scala.concurrent.{ExecutionContext, Future}

import connectors.Reference // move to model
import models.fileupload._
import org.bson.types.ObjectId
import repositories.FileUploadRepository

class MongoBackedUploadProgressTracker @Inject() (repository: FileUploadRepository)(implicit
  ec: ExecutionContext
) extends UploadProgressTracker {

  override def requestUpload(uploadId: UploadId, fileReference: Reference): Future[Unit] =
    repository.insert(UploadDetails(ObjectId.get(), uploadId, fileReference, InProgress))

  override def registerUploadResult(
    fileReference: Reference,
    uploadStatus: UploadStatus
  ): Future[Unit] =
    repository.updateStatus(fileReference, uploadStatus).map(_ => ())

  override def getUploadResult(id: UploadId): Future[Option[UploadStatus]] =
    for (result <- repository.findByUploadId(id)) yield result.map(_.status)
}
