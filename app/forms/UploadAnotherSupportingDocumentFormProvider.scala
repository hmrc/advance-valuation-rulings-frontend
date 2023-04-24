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

import javax.inject.Inject

import play.api.Configuration
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

import forms.mappings.Mappings
import models.DraftAttachment

class UploadAnotherSupportingDocumentFormProvider @Inject() (
  configuration: Configuration
) extends Mappings {

  private val maxFiles: Int = configuration.get[Int]("upscan.maxFiles")

  def apply(documents: Seq[DraftAttachment]): Form[Boolean] =
    Form(
      "value" -> boolean("uploadAnotherSupportingDocument.error.required")
        .verifying(fileCountConstraint(documents))
    )

  private def fileCountConstraint(documents: Seq[DraftAttachment]): Constraint[Boolean] =
    Constraint {
      value =>
        if (!value || documents.size < maxFiles) {
          Valid
        } else {
          Invalid("uploadAnotherSupportingDocument.error.fileCount", maxFiles)
        }
    }
}
