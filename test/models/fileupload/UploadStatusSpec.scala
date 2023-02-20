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

import base.SpecBase

class UploadStatusSpec extends SpecBase {

  "UploadStatus" - {
    val successStatus    = UploadedSuccessfully("name", "mimeType", "downloadUrl", Some(1L))

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

      "be false for not started" in {
        UploadStatus.isError(NotStarted) mustBe false
      }

      "be false for in progress" in {
        UploadStatus.isError(InProgress) mustBe false
      }
    }

    "toFormErrors must" - {
      "create empty error map for not started" in {
        UploadStatus.toFormErrors(NotStarted) mustBe Map.empty
      }

      "create empty error map for in progress" in {
        UploadStatus.toFormErrors(InProgress) mustBe Map.empty
      }

      "create empty error map for uploaded successfully" in {
        UploadStatus.toFormErrors(successStatus) mustBe Map.empty
      }

      "create form error map with failed message for failed" in {
        UploadStatus.toFormErrors(Failed) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.failed"
        )
      }

      "create map with rejected message for rejected" in {
        UploadStatus.toFormErrors(Rejected) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.rejected"
        )
      }

      "create map with quarantine message for quarantine" in {
        UploadStatus.toFormErrors(Quarantine) mustBe Map(
          "file-input" -> "uploadSupportingDocuments.quarantine"
        )
      }
    }
  }
}
