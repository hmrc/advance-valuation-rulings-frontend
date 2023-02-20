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

package models.fileupload

import java.util.UUID

import play.api.libs.json._
import play.api.mvc.QueryStringBindable

case class UploadId(value: String) extends AnyVal

object UploadId {
  def generate = UploadId(UUID.randomUUID().toString)

  implicit def queryBinder(implicit
    stringBinder: QueryStringBindable[String]
  ): QueryStringBindable[UploadId] =
    stringBinder.transform(UploadId(_), _.value)

  implicit val idFormat: Format[UploadId] =
    Format(
      Reads.StringReads.map(UploadId(_)),
      Writes.StringWrites.contramap[UploadId](_.value)
    )
}

sealed trait FileUploadIds {
  def nextUploadFileId: UploadId
  def redirectUrlFileId: UploadId
}
object FileUploadIds {
  def generateNewFileUploadId                  = NewFileUpload(UploadId.generate)
  def fromExistingUploadId(uploadId: UploadId) = ExistingFileUpload(uploadId, UploadId.generate)
}
case class NewFileUpload(nextFileId: UploadId) extends FileUploadIds {
  def redirectUrlFileId: UploadId = nextFileId
  def nextUploadFileId: UploadId  = nextFileId
}
case class ExistingFileUpload(existingFileId: UploadId, nextFileId: UploadId)
    extends FileUploadIds {
  def redirectUrlFileId: UploadId = existingFileId
  def nextUploadFileId: UploadId  = nextFileId
}
