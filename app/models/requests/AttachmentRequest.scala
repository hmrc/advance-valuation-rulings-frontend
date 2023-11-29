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
import models.UploadedFile._
import models.{DraftAttachment, Index, UploadedFile, UserAnswers}
import pages._
import play.api.libs.json.{Json, OFormat}
import queries.{AllDocuments, DraftAttachmentAt}

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
      case true =>
        answers
          .validated(AllDocuments)
          .andThen(_.zipWithIndex.traverse { case (document, i) =>
            val validatedPrivacy = checkFilePrivacy(document, i)
            val validatedFile    = checkFile(document, i)

            (validatedFile, validatedPrivacy).mapN { case (Success(_, downloadUrl, uploadDetails), isConfidential) =>
              AttachmentRequest(
                name = uploadDetails.fileName,
                description = None,
                url = downloadUrl,
                privacy = if (isConfidential) {
                  Privacy.Confidential
                } else {
                  Privacy.Public
                },
                mimeType = uploadDetails.fileMimeType,
                size = uploadDetails.size
              )
            }
          })

      case false =>
        Validated.Valid(Nil)
    }

  private def checkFile(
    document: DraftAttachment,
    index: Int
  ): ValidatedNel[Page, UploadedFile.Success] =
    document.file match {
      case Failure(_, _) => DraftAttachmentAt(Index(index)).invalidNel
      case Initiated(_)  => DraftAttachmentAt(Index(index)).invalidNel
      case s: Success    => s.validNel
    }

  private def checkFilePrivacy(document: DraftAttachment, index: Int): ValidatedNel[Page, Boolean] =
    document.isThisFileConfidential.toValidNel(DraftAttachmentAt(Index(index)))
}
