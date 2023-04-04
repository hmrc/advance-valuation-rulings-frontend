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

import play.api.i18n.Messages

import base.SpecBase

class UploadStatusSpec extends SpecBase {

  "UploadStatus" - {
    val successStatus =
      UploadedSuccessfully("name", "mimeType", "downloadUrl", "checksum", Some(1L))

    "is error must" - {

      "be true for failed" in {
        UploadStatus.isError(Failed) mustBe true
      }

      "be true for rejected" in {
        UploadStatus.isError(Rejected) mustBe true
      }

      "be true for quarantine" in {
        UploadStatus.isError(Quarantine) mustBe true
      }

      "be true for no file provided" in {
        UploadStatus.isError(NoFileProvided) mustBe true
      }

      "be true for entity too large" in {
        UploadStatus.isError(EntityTooLarge) mustBe true
      }

      "be true for entity too small" in {
        UploadStatus.isError(EntityTooSmall) mustBe true
      }

      "be false for not started" in {
        UploadStatus.isError(NotStarted) mustBe false
      }

      "be false for in progress" in {
        UploadStatus.isError(InProgress) mustBe false
      }

      "be false for uploaded successfully" in {
        UploadStatus.isError(successStatus) mustBe false
      }
    }

    "toFormErrors must" - {
      val MaxFileSize            = 10
      implicit val msg: Messages = messages(applicationBuilder().build())

      "create empty error map for not started" in {
        UploadStatus.toFormErrors(NotStarted, MaxFileSize) mustBe Map.empty
      }

      "create empty error map for in progress" in {
        UploadStatus.toFormErrors(InProgress, MaxFileSize) mustBe Map.empty
      }

      "create empty error map for uploaded successfully" in {
        UploadStatus.toFormErrors(successStatus, MaxFileSize) mustBe Map.empty
      }

      "create form error map with failed message for failed" in {
        UploadStatus.toFormErrors(Failed, MaxFileSize) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.failed"
        )
      }

      "create map with rejected message for rejected" in {
        UploadStatus.toFormErrors(Rejected, MaxFileSize) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.rejected"
        )
      }

      "create map with quarantine message for quarantine" in {
        UploadStatus.toFormErrors(Quarantine, MaxFileSize) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.quarantine"
        )
      }

      "create map with no file provided message for no file provided" in {
        UploadStatus.toFormErrors(NoFileProvided, MaxFileSize) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.nofileprovided"
        )
      }

      "create map with entity too large message for entity too large" in {
        UploadStatus.toFormErrors(EntityTooLarge, MaxFileSize) mustBe Map(
          "file-input" -> msg("uploadSupportingDocuments.entitytoolarge", MaxFileSize)
        )
      }

      "create map with entity too small message for entity too small" in {
        UploadStatus.toFormErrors(EntityTooSmall, MaxFileSize) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.entitytoosmall"
        )
      }

      "create map with duplicate file message for duplicate file" in {
        UploadStatus.toFormErrors(DuplicateFile, MaxFileSize) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.duplicatefile"
        )
      }
    }
  }
}
