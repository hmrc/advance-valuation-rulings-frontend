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

package forms

import config.FrontendAppConfig
import forms.behaviours.BooleanFieldBehaviours
import models.{DraftAttachment, UploadedFile}
import org.mockito.Mockito.{mock, when}
import play.api.data.FormError

import java.time.Instant

class UploadAnotherSupportingDocumentFormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "uploadAnotherSupportingDocument.error.required"
  private val invalidKey  = "error.boolean"

  private val mockFrontendAppConfig = mock(classOf[FrontendAppConfig])

  when(mockFrontendAppConfig.maxFiles)
    .thenReturn(1)

  val formProvider = new UploadAnotherSupportingDocumentFormProvider(mockFrontendAppConfig)

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

    "must succeed when the user answers no when there are already the max number of files" in {

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
