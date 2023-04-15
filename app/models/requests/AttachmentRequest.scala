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

import cats.data.{Validated, ValidatedNel}

import play.api.libs.json.{Json, OFormat}

import models.{Index, UserAnswers}
import pages.{DoYouWantToUploadDocumentsPage, Page, UploadSupportingDocumentPage}

final case class AttachmentRequest(
  name: String,
  description: Option[String],
  url: String,
  privacy: Privacy,
  mimeType: String,
  size: Long
)

object AttachmentRequest {

  implicit lazy val format: OFormat[AttachmentRequest] = Json.format

  def apply(answers: UserAnswers): ValidatedNel[Page, Seq[AttachmentRequest]] =
    Validated.Valid(Nil)
//    answers
//      .validated(DoYouWantToUploadDocumentsPage)
//      .andThen {
//        case true =>
//          answers
//            .validated(UploadSupportingDocumentPage(Index(0))) // TODO fix
//            .map(_.files.toSeq.map {
//              case (_, file) =>
//                AttachmentRequest(
//                  name = file.fileName,
//                  description = None,
//                  url = file.downloadUrl,
//                  privacy = if (file.isConfidential) Privacy.Confidential else Privacy.Public,
//                  mimeType = file.mimeType,
//                  size = file.size
//                )
//            })
//
//        case false =>
//          Validated.Valid(Nil)
//      }
}
