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

import play.api.libs.json.{__, OFormat, OWrites, Reads}

import models.fileupload.UploadId

case class IsThisFileConfidential(
  uploadId: UploadId,
  isConfidential: Boolean,
  downloadUrl: String, // URL?
  fileName: String
)

object IsThisFileConfidential {

  val reads: Reads[IsThisFileConfidential] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "uploadId").read[String].map(UploadId(_)) and
        (__ \ "isConfidential").read[Boolean] and
        (__ \ "downloadUrl").read[String] and
        (__ \ "fileName").read[String]
    )(IsThisFileConfidential.apply _)
  }

  val writes: OWrites[IsThisFileConfidential] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "uploadId").write[String] and
        (__ \ "isConfidential").write[Boolean] and
        (__ \ "downloadUrl").write[String] and
        (__ \ "fileName").write[String]
    )(
      unlift(
        (isFileConfidential: IsThisFileConfidential) =>
          Some(
            (
              isFileConfidential.uploadId.value,
              isFileConfidential.isConfidential,
              isFileConfidential.downloadUrl,
              isFileConfidential.fileName
            )
          )
      )
    )
  }

  implicit val format: OFormat[IsThisFileConfidential] = OFormat(reads, writes)
}
