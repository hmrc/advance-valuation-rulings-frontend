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

package viewmodels.checkAnswers

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models._
import models.fileupload._
import pages.{DoYouWantToUploadDocumentsPage, IsThisFileConfidentialPage, UploadSupportingDocumentPage}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DoYouWantToUploadDocumentsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DoYouWantToUploadDocumentsPage).map {
      answer =>
        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "doYouWantToUploadDocuments.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.DoYouWantToUploadDocumentsController.onPageLoad(CheckMode).url
            )
              .withVisuallyHiddenText(messages("doYouWantToUploadDocuments.change.hidden"))
          )
        )
    }
}

object UploadedDocumentsSummary {

  def row(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val isConfidential: Option[FileConfidentiality] =
      userAnswers.get(IsThisFileConfidentialPage)

    val upscanDetails: Option[UploadedFiles] =
      userAnswers.get(UploadSupportingDocumentPage)

    val documentsOption = for {
      fileConfidentiality <- isConfidential
      uploadedFiles       <- upscanDetails
      supportingDocuments  = SupportingDocument.makeFromAnswers(uploadedFiles, fileConfidentiality)
    } yield supportingDocuments
    documentsOption
      .map(documents => documents.map(_.fileName).mkString(", "))
      .map(
        (answer: String) =>
          SummaryListRowViewModel(
            key = "uploadSupportingDocuments.checkYourAnswersLabel",
            value = ValueViewModel(answer),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.UploadAnotherSupportingDocumentController.onPageLoad(CheckMode).url
              )
                .withVisuallyHiddenText(messages("doYouWantToUploadDocuments.change.hidden"))
            )
          )
      )
  }
}
