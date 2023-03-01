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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.{__, OFormat, OWrites, Reads}

import models.IsThisFileConfidential

case class UploadedFiles(lastUploadId: Option[UploadId], files: Map[UploadId, UpscanFileDetails]) {
  def addFile(upscanFileDetails: UpscanFileDetails): UploadedFiles =
    UploadedFiles(
      Some(upscanFileDetails.uploadId),
      files + (upscanFileDetails.uploadId -> upscanFileDetails)
    )

  def removeFile(uploadId: UploadId): UploadedFiles = {
    val lastUploadId = if (this.lastUploadId.contains(uploadId)) None else this.lastUploadId
    UploadedFiles(lastUploadId, files - uploadId)
  }

  def fileCount: Int = files.size
}

object UploadedFiles {
  implicit val detailsMapReads: Reads[Map[UploadId, UpscanFileDetails]] = Reads
    .map[UpscanFileDetails]
    .map(
      (values: Map[String, UpscanFileDetails]) => values.map { case ((k, v)) => (UploadId(k), v) }
    )
  implicit val reads: Reads[UploadedFiles]                              = (
    (JsPath \ "lastUploadId").readNullable[String].map(_.map(UploadId(_))) and
      (JsPath \ "files").read[Map[UploadId, UpscanFileDetails]]
  ).apply(UploadedFiles.apply(_, _))

  implicit val detailsMapWrites: Writes[Map[UploadId, UpscanFileDetails]] = OWrites
    .map[UpscanFileDetails]
    .contramap((uf: Map[UploadId, UpscanFileDetails]) => uf.map { case ((k, v)) => (k.value, v) })

  implicit val writes: OWrites[UploadedFiles] = (
    (JsPath \ "lastUploadId").writeOptionWithNull[String] and
      (JsPath \ "files").write[Map[UploadId, UpscanFileDetails]]
  )(
    (uploadedFiles: UploadedFiles) => (uploadedFiles.lastUploadId.map(_.value), uploadedFiles.files)
  )

  implicit val format: OFormat[UploadedFiles] = OFormat(
    reads,
    writes
  )

  def apply(file: UpscanFileDetails): UploadedFiles =
    UploadedFiles(Some(file.uploadId), Map(file.uploadId -> file))
}

case class FileConfidentiality(files: Map[UploadId, Boolean]) extends AnyVal {
  def setConfidentiality(fileInfo: IsThisFileConfidential): FileConfidentiality =
    FileConfidentiality(files + (fileInfo.uploadId -> fileInfo.isConfidential))

  def setConfidentiality(uploadId: UploadId, confidential: Boolean): FileConfidentiality =
    FileConfidentiality(files + (uploadId -> confidential))

  def removeFile(uploadId: UploadId): FileConfidentiality =
    FileConfidentiality(files - uploadId)

  def forFile(uploadId: UploadId): Option[Boolean] = files.get(uploadId)
}

object FileConfidentiality {
  def apply(isThisFileConfidential: IsThisFileConfidential): FileConfidentiality =
    FileConfidentiality(
      Map(isThisFileConfidential.uploadId -> isThisFileConfidential.isConfidential)
    )

  implicit val format: OFormat[FileConfidentiality] = OFormat(
    Reads
      .map[Boolean]
      .map(
        (values: Map[String, Boolean]) =>
          FileConfidentiality.apply(values.map { case ((k, v)) => (UploadId(k), v) })
      ),
    OWrites
      .map[Boolean]
      .contramap((uf: FileConfidentiality) => uf.files.map { case ((k, v)) => (k.value, v) })
  )

}

case class UpscanFileDetails(
  uploadId: UploadId,
  fileName: String,
  downloadUrl: String
)

object UpscanFileDetails {

  val reads: Reads[UpscanFileDetails] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "uploadId").read[UploadId] and
        (__ \ "fileName").read[String] and
        (__ \ "downloadUrl").read[String]
    )(UpscanFileDetails.apply _)
  }

  val writes: OWrites[UpscanFileDetails] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "uploadId").write[UploadId] and
        (__ \ "fileName").write[String] and
        (__ \ "downloadUrl").write[String]
    )(unlift(UpscanFileDetails.unapply))
  }

  implicit val format: OFormat[UpscanFileDetails] = OFormat(reads, writes)
}
