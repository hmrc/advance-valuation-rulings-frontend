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

package models.requests

import cats.data.Validated
import cats.data.ValidatedNel

import play.api.libs.json._

import models.UserAnswers
import pages._

case class Attachment(
  id: String,
  name: String,
  url: String,
  public: Boolean, // isConfidential
  mimeType: String,
  size: Long
)
object Attachment {
  implicit val format: OFormat[Attachment] = Json.format[Attachment]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, Seq[Attachment]] =
    userAnswers
      .validated(DoYouWantToUploadDocumentsPage)
      .andThen {
        hasDocuments =>
          hasDocuments match {
            case true  =>
              userAnswers
                .validated(UploadSupportingDocumentPage)
                .map(
                  uploads =>
                    uploads.files.toSeq.map {
                      case (id, file) =>
                        Attachment(
                          id = id.value,
                          name = file.fileName,
                          url = file.downloadUrl,
                          public = !file.isConfidential,
                          mimeType = file.mimeType,
                          size = file.size
                        )
                    }
                )
            case false => Validated.Valid(Seq.empty[Attachment])
          }
      }
}
