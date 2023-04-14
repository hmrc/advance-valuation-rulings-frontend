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

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}

import models.{DraftId, UploadedFile, UploadedFiles, UserAnswers}
import models.fileupload.UploadId
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.{DoYouWantToUploadDocumentsPage, UploadSupportingDocumentPage}

class AttachmentRequestSpec extends AnyWordSpec with Matchers with TryValues with OptionValues {

  private val draftId: DraftId              = DraftId(1)
  private val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

  ".apply" must {

    "return an empty sequence when the user doesn't want to add any attachments" in {

      val answers = emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, false).success.value

      val result = AttachmentRequest(answers)

      result mustBe Valid(Nil)
    }

    "return attachment requests when the user wants to add attachments" in {

      val files = UploadedFiles(
        lastUpload = None,
        files = Map(
          UploadId("1") -> UploadedFile(
            "name",
            "url",
            isConfidential = true,
            "mime",
            1337
          )
        )
      )

      val answers =
        emptyUserAnswers
          .set(DoYouWantToUploadDocumentsPage, true)
          .success
          .value
          .set(UploadSupportingDocumentPage, files)
          .success
          .value

      val result = AttachmentRequest(answers)

      result mustEqual Valid(
        Seq(
          AttachmentRequest(
            name = "name",
            description = None,
            url = "url",
            privacy = Privacy.Confidential,
            mimeType = "mime",
            size = 1337
          )
        )
      )
    }

    "fail when the user says they want to upload files but none are present" in {

      val answers = emptyUserAnswers.set(DoYouWantToUploadDocumentsPage, true).success.value

      val result = AttachmentRequest(answers)

      result mustBe Invalid(NonEmptyList.one(UploadSupportingDocumentPage))
    }
  }
}
