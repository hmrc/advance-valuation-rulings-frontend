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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

import models.fileupload._

case class UploadedFiles(
  lastUpload: Option[UpscanFileDetails],
  files: Map[UploadId, UploadedFile]
) {
  def addFile(upscanFileDetails: UpscanFileDetails): UploadedFiles =
    UploadedFiles(
      Some(upscanFileDetails),
      files
    )

  def getFile(uploadId: UploadId): Option[UploadedFile] = files.get(uploadId)

  def setConfidentiality(isConfidential: Boolean): UploadedFiles =
    this.lastUpload match {
      case Some(lastUpload) =>
        val newFile = UploadedFile(
          lastUpload.fileName,
          lastUpload.downloadUrl,
          isConfidential,
          lastUpload.mimeType,
          lastUpload.size
        )
        UploadedFiles(
          None,
          files + (lastUpload.uploadId -> newFile)
        )
      case None             => this
    }

  def removeFile(uploadId: UploadId): UploadedFiles = {
    val lastUploadId = if (this.lastUpload.contains(uploadId)) None else this.lastUpload
    UploadedFiles(lastUploadId, files - uploadId)
  }

  def fileCount: Int = files.size
}

case class UploadedFile(
  fileName: String,
  downloadUrl: String,
  isConfidential: Boolean,
  mimeType: String,
  size: Long
)
object UploadedFile {
  implicit val reads  = Json.reads[UploadedFile]
  implicit val writes = Json.writes[UploadedFile]
  implicit val format = OFormat(reads, writes)
}

object UploadedFiles {
  implicit val readsFiles: Reads[scala.collection.immutable.Map[UploadId, UploadedFile]] = Reads
    .map[UploadedFile]
    .map((values: Map[String, UploadedFile]) => values.map { case ((k, v)) => (UploadId(k), v) })
  implicit val writesFiles: Writes[Map[UploadId, UploadedFile]]                          = OWrites
    .map[UploadedFile]
    .contramap((uf: Map[UploadId, UploadedFile]) => uf.map { case ((k, v)) => (k.value, v) })

  implicit val reads                          = Json.reads[UploadedFiles]
  implicit val writes: OWrites[UploadedFiles] = (
    (JsPath \ "lastUpload").writeOptionWithNull[UpscanFileDetails] and
      (JsPath \ "files").write[Map[UploadId, UploadedFile]]
  )((uploadedFiles: UploadedFiles) => (uploadedFiles.lastUpload, uploadedFiles.files))

  implicit val format = OFormat(reads, writes)

  def initialise(file: UpscanFileDetails) =
    UploadedFiles(Some(file), Map.empty)
}
