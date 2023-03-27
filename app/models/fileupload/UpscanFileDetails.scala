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

import play.api.libs.json.{__, OFormat, OWrites, Reads}

case class UpscanFileDetails(
  uploadId: UploadId,
  fileName: String,
  downloadUrl: String,
  mimeType: String,
  size: Long
)

object UpscanFileDetails {

  val reads: Reads[UpscanFileDetails] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "uploadId").read[UploadId] and
        (__ \ "fileName").read[String] and
        (__ \ "downloadUrl").read[String] and
        (__ \ "mimeType").read[String] and
        (__ \ "size").read[Long]
    )(UpscanFileDetails.apply _)
  }

  val writes: OWrites[UpscanFileDetails] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "uploadId").write[UploadId] and
        (__ \ "fileName").write[String] and
        (__ \ "downloadUrl").write[String] and
        (__ \ "mimeType").write[String] and
        (__ \ "size").write[Long]
    )(unlift(UpscanFileDetails.unapply))
  }

  implicit val format: OFormat[UpscanFileDetails] = OFormat(reads, writes)
}
