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
import cats.implicits._

import play.api.libs.json.{Json, OFormat}

import models.{Index, UploadedFile, UserAnswers}
import pages.{DoYouWantToUploadDocumentsPage, IsThisFileConfidentialPage, Page, UploadSupportingDocumentPage}
import queries.AllDocuments

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
    answers.validated(DoYouWantToUploadDocumentsPage).andThen {
      case true  =>
        answers.get(AllDocuments).toValidNel(UploadSupportingDocumentPage(Index(0))).andThen {
          _.indices.toList.traverse {
            i =>
              (getFile(answers, i), getFilePrivacy(answers, i)).mapN {
                (file, isConfidential) =>
                  new AttachmentRequest(
                    name = file.uploadDetails.fileName,
                    description = None,
                    url = file.downloadUrl,
                    privacy = if (isConfidential) Privacy.Confidential else Privacy.Public,
                    mimeType = file.uploadDetails.fileMimeType,
                    size = file.uploadDetails.size
                  )
              }
          }
        }
      case false =>
        Validated.Valid(Nil)
    }

  private def getFile(answers: UserAnswers, index: Int): ValidatedNel[Page, UploadedFile.Success] =
    answers.validated(UploadSupportingDocumentPage(Index(index))).andThen {
      case file: UploadedFile.Success => file.validNel
      case _                          => UploadSupportingDocumentPage(Index(index)).invalidNel
    }

  private def getFilePrivacy(answers: UserAnswers, index: Int): ValidatedNel[Page, Boolean] =
    answers.validated(IsThisFileConfidentialPage(Index(index)))
}
