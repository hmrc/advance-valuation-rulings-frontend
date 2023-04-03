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
import play.api.libs.json.{__, OFormat, OWrites, Reads}
import uk.gov.hmrc.govukfrontend.views.Aliases.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

import viewmodels.govuk.TableFluency

case class UploadAnotherSupportingDocument(
  value: Boolean,
  fileCount: Int
)
object UploadAnotherSupportingDocument {

  val reads: Reads[UploadAnotherSupportingDocument] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "value").read[Boolean] and
        (__ \ "fileCount").read[Int]
    )(UploadAnotherSupportingDocument.apply _)
  }

  val writes: OWrites[UploadAnotherSupportingDocument] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "value").write[Boolean] and
        (__ \ "fileCount").write[Int]
    )(unlift(UploadAnotherSupportingDocument.unapply))
  }

  implicit val format: OFormat[UploadAnotherSupportingDocument] = OFormat(reads, writes)
}

case class SupportingDocument(uploadId: String, fileName: String, isConfidential: Boolean)
object SupportingDocument {
  def makeForFiles(uploadedFiles: UploadedFiles): Seq[SupportingDocument] =
    uploadedFiles.files.map {
      case (fileId, fileDetails) =>
        SupportingDocument(fileId.value, fileDetails.fileName, fileDetails.isConfidential)
    }.toSeq
}

object SupportingDocumentsRows extends TableFluency {
  def apply(
    uploadedFiles: UploadedFiles,
    link: views.html.components.Link,
    mode: Mode
  )(implicit messages: Messages): Table = {
    val rows: Seq[Seq[TableRow]] =
      uploadedFiles.files.map {
        case (fileId, fileDetails) =>
          Seq(
            TableRow(content = Text(fileDetails.fileName)),
            TableRow(content = Text(confidentialLabel(fileDetails))),
            TableRow(
              HtmlContent(
                link(
                  id = fileId.value,
                  text = messages("site.remove"),
                  call = controllers.routes.UploadAnotherSupportingDocumentController
                    .onDelete(fileId.value, mode),
                  classes = "govuk-link govuk-link--no-visited-state"
                )
              )
            )
          )
      }.toSeq

    Table(rows.withCssClass("govuk-body-s"))
  }

  private def confidentialLabel(fileDetails: UploadedFile)(implicit messages: Messages): String =
    if (fileDetails.isConfidential) {
      messages("uploadAnotherSupportingDocument.keepConfidential")
    } else {
      ""
    }
}
