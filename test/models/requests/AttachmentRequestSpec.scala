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

import java.time.Instant

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}

import models.{DraftId, Index, UploadedFile, UserAnswers}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages._

class AttachmentRequestSpec extends AnyWordSpec with Matchers with TryValues with OptionValues {

  private val draftId: DraftId              = DraftId(1)
  private val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

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

  ".apply" must {

    "return an empty sequence when the user doesn't want to add any attachments" in {

      val answers = emptyUserAnswers
        .set(DoYouWantToUploadDocumentsPage, false)
        .success
        .value
        .set(UploadedFilePage(Index(0)), successfulFile)
        .success
        .value
        .set(IsThisFileConfidentialPage(Index(0)), false)
        .success
        .value

      val result = AttachmentRequest(answers)

      result mustBe Valid(Nil)
    }

    "return attachment requests when the user wants to add attachments" in {

      val answers =
        emptyUserAnswers
          .set(DoYouWantToUploadDocumentsPage, true)
          .success
          .value
          .set(UploadedFilePage(Index(0)), successfulFile)
          .success
          .value
          .set(IsThisFileConfidentialPage(Index(0)), false)
          .success
          .value
          .set(UploadedFilePage(Index(1)), successfulFile)
          .success
          .value
          .set(IsThisFileConfidentialPage(Index(1)), true)
          .success
          .value

      val result = AttachmentRequest(answers)

      result mustEqual Valid(
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

    "fail when the user says they want to upload files but none are present" in {

      val answers = emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true).success.value
      val result  = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(UploadedFilePage(Index(0))))
    }

    "fail when there are non-successful files" in {

      val answers = emptyUserAnswers
        .set(DoYouWantToUploadDocumentsPage, true)
        .success
        .value
        .set(UploadedFilePage(Index(0)), failedFile)
        .success
        .value
        .set(IsThisFileConfidentialPage(Index(0)), true)
        .success
        .value
      val result  = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(UploadedFilePage(Index(0))))
    }

    "fail when privacy setting is missing from files" in {

      val answers = emptyUserAnswers
        .set(DoYouWantToUploadDocumentsPage, true)
        .success
        .value
        .set(UploadedFilePage(Index(0)), successfulFile)
        .success
        .value
      val result  = AttachmentRequest(answers)
      result mustBe Invalid(NonEmptyList.one(IsThisFileConfidentialPage(Index(0))))
    }
  }
}
