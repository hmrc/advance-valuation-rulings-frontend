/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import cats.data.{NonEmptyList, ValidatedNel}
import cats.data.Validated.{Invalid, Valid}
import models.{DraftAttachment, Index, UploadedFile, UserAnswers}
import pages._
import queries.{AllDocuments, DraftAttachmentAt}

import java.time.Instant

class AttachmentRequestSpec extends SpecBase {

  private val successfulFile = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1337
    )
  )

  private val failedFile = UploadedFile.Failure(
    reference = "reference",
    failureDetails = UploadedFile.FailureDetails(
      failureReason = UploadedFile.FailureReason.Quarantine,
      failureMessage = Some("failureMessage")
    )
  )

  private val initiatedFile: UploadedFile.Initiated = UploadedFile.Initiated(reference = "reference")

  ".apply" - {

    "must return an empty sequence when the user doesn't want to add any attachments" in {

      val answers: UserAnswers = (for {
        ua <- emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, false)
        ua <- ua.set(DraftAttachmentAt(Index(0)), DraftAttachment(successfulFile, Some(false)))
      } yield ua).success.value

      val result: ValidatedNel[Page, Seq[AttachmentRequest]] = AttachmentRequest(answers)

      result mustBe Valid(Nil)
    }

    "must return attachment requests when the user wants to add attachments" in {
      val answers: UserAnswers = (for {
        ua <- emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true)
        ua <- ua.set(DraftAttachmentAt(Index(0)), DraftAttachment(successfulFile, Some(false)))
        ua <- ua.set(DraftAttachmentAt(Index(1)), DraftAttachment(successfulFile, Some(true)))
      } yield ua).success.value

      val result: ValidatedNel[Page, Seq[AttachmentRequest]] = AttachmentRequest(answers)

      result mustBe Valid(
        Seq(
          AttachmentRequest(
            name = "fileName",
            description = None,
            url = "downloadUrl",
            privacy = Privacy.Public,
            mimeType = "fileMimeType",
            size = 1337
          ),
          AttachmentRequest(
            name = "fileName",
            description = None,
            url = "downloadUrl",
            privacy = Privacy.Confidential,
            mimeType = "fileMimeType",
            size = 1337
          )
        )
      )
    }

    "must fail when the user says they want to upload files but none are present" in {

      val answers: UserAnswers                               = emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true).success.value
      val result: ValidatedNel[Page, Seq[AttachmentRequest]] = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(AllDocuments))
    }

    "must fail when there are initiated files" in {
      val answers: UserAnswers = (for {
        ua <- emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true)
        ua <- ua.set(DraftAttachmentAt(Index(0)), DraftAttachment(initiatedFile, Some(true)))
      } yield ua).success.value

      val result: ValidatedNel[Page, Seq[AttachmentRequest]] = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(DraftAttachmentAt(Index(0))))
    }

    "must fail when there are non-successful files" in {
      val answers: UserAnswers = (for {
        ua <- emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true)
        ua <- ua.set(DraftAttachmentAt(Index(0)), DraftAttachment(failedFile, Some(true)))
      } yield ua).success.value

      val result: ValidatedNel[Page, Seq[AttachmentRequest]] = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(DraftAttachmentAt(Index(0))))
    }

    "must fail when privacy setting is missing from files" in {
      val answers: UserAnswers = (for {
        ua <- emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true)
        ua <- ua.set(DraftAttachmentAt(Index(0)), DraftAttachment(successfulFile, None))
      } yield ua).success.value

      val result: ValidatedNel[Page, Seq[AttachmentRequest]] = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(DraftAttachmentAt(Index(0))))
    }
  }
}
