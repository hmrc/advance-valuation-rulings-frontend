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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table._

import models.fileupload._

case class SupportingDocument(uploadId: String, fileName: String, isConfidential: Boolean)
object SupportingDocument {
  // TODO rename
  def makeFromAnswers(
    uploadedFiles: UploadedFiles,
    fileConfidentiality: FileConfidentiality
  ): Seq[SupportingDocument] =
    uploadedFiles.files.map {
      case (fileId, fileDetails) =>
        val isConfidential = fileConfidentiality.files
          .get(fileId)
          // .find(_.uploadId == file.id)
          .getOrElse(false)
        // TODO could be option and not show
        SupportingDocument(fileId.value, fileDetails.fileName, isConfidential)
    }.toSeq
}
object SupportingDocumentsTable {

  def apply(
    uploadedFiles: UploadedFiles,
    fileConfidentiality: FileConfidentiality
  )(implicit messages: Messages): Table = {

    val supportingDocuments: Seq[SupportingDocument] = SupportingDocument.makeFromAnswers(
      uploadedFiles,
      fileConfidentiality
    )

    Table(
      head = None,
      rows = supportingDocuments.map {
        document =>
          Seq(
            TableRow(content = Text(document.fileName)),
            TableRow(content =
              Text(
                if (document.isConfidential)
                  messages("uploadAnotherSupportingDocument.keepConfidential")
                else ""
              )
            ),
            TableRow(content = Text(messages("site.remove")))
          )
      }
    )
  }
}
