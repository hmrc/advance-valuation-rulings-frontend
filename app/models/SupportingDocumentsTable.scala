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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table._

import viewmodels.govuk.summarylist._
import viewmodels.implicits._

case class SupportingDocument(uploadId: String, fileName: String, isConfidential: Boolean)
object SupportingDocument {
  def makeForFiles(uploadedFiles: UploadedFiles): Seq[SupportingDocument] =
    uploadedFiles.files.map {
      case (fileId, fileDetails) =>
        SupportingDocument(fileId.value, fileDetails.fileName, fileDetails.isConfidential)
    }.toSeq
}

object SupportingDocumentsTable {
  def apply(
    uploadedFiles: UploadedFiles,
    link: views.html.components.Link
  )(implicit messages: Messages): Table = {

    val supportingDocuments: Seq[SupportingDocument] = SupportingDocument.makeForFiles(
      uploadedFiles
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
            TableRow(
              content = HtmlContent(
                {
                  link(
                    id = document.uploadId,
                    text = messages("site.remove"),
                    call = play.api.mvc.Call(
                      "DELETE",
                      controllers.routes.UploadAnotherSupportingDocumentController
                        .onDelete(document.uploadId)
                        .url
                    )
                  )
                }
                // s"""<a href="${controllers.routes.UploadAnotherSupportingDocumentController
                //     .onDelete(document.uploadId)
                //     .url}">${messages("site.remove")}</a>"""
              )
            )
          )
      }
    )
  }
}

object SupportingDocumentsRows {
  def apply(
    uploadedFiles: UploadedFiles,
    link: views.html.components.Link
  )(implicit messages: Messages): SummaryList = {
    val rows =
      uploadedFiles.files.map {
        case (fileId, fileDetails) =>
          SummaryListRowViewModel(
            key = KeyViewModel(
              content = Text(fileDetails.fileName)
            ),
            value = ValueViewModel(
              content = Text(
                if (fileDetails.isConfidential)
                  messages("uploadAnotherSupportingDocument.keepConfidential")
                else ""
              )
            ),
            actions = Seq(
              ActionItemViewModel(
                "site.remove",
                controllers.routes.UploadAnotherSupportingDocumentController
                  .onDelete(fileId.value)
                  .url
              )
                .withVisuallyHiddenText(messages("site.remove"))
            )
          )
      }.toSeq

    SummaryList(rows)

  }
}
