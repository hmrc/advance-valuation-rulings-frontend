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

package forms

import java.time.Instant

import play.api.Configuration
import play.api.data.FormError

import forms.behaviours.BooleanFieldBehaviours
import models.{DraftAttachment, UploadedFile}

class UploadAnotherSupportingDocumentFormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "uploadAnotherSupportingDocument.error.required"
  private val invalidKey  = "error.boolean"

  private val configuration = Configuration(
    "upscan.maxFiles" -> 1
  )

  val formProvider = new UploadAnotherSupportingDocumentFormProvider(configuration)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      formProvider(Seq.empty),
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      formProvider(Seq.empty),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must fail when the user answers yes and there are already the max number of files" in {

      val file = UploadedFile.Success(
        reference = "reference",
        downloadUrl = "foobar",
        uploadDetails = UploadedFile.UploadDetails(
          fileName = "filename",
          fileMimeType = "application/pdf",
          uploadTimestamp = Instant.now(),
          checksum = "checksum",
          size = 1337
        )
      )

      val form      = formProvider(Seq(DraftAttachment(file, None)))
      val boundForm = form.bind(Map("value" -> "true"))

      boundForm.hasErrors mustBe true
      boundForm("value").error.value mustEqual FormError(
        "value",
        "uploadAnotherSupportingDocument.error.fileCount",
        Seq(1)
      )
    }

    "must succeed when the uer answers no when there are already the max number of files" in {

      val file = UploadedFile.Success(
        reference = "reference",
        downloadUrl = "foobar",
        uploadDetails = UploadedFile.UploadDetails(
          fileName = "filename",
          fileMimeType = "application/pdf",
          uploadTimestamp = Instant.now(),
          checksum = "checksum",
          size = 1337
        )
      )

      val form      = formProvider(Seq(DraftAttachment(file, None)))
      val boundForm = form.bind(Map("value" -> "false"))

      boundForm.hasErrors mustBe false
    }
  }
}
