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

package viewmodels.checkAnswers

import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models._
import pages.DoYouWantToUploadDocumentsPage
import queries.AllDocuments
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DoYouWantToUploadDocumentsSummary {

  def row(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers.get(DoYouWantToUploadDocumentsPage).map { answer =>
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = "doYouWantToUploadDocuments.checkYourAnswersLabel",
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.DoYouWantToUploadDocumentsController
              .onPageLoad(CheckMode, userAnswers.draftId)
              .url
          )
            .withVisuallyHiddenText(messages("doYouWantToUploadDocuments.change.hidden"))
        )
      )
    }
}

object UploadedDocumentsSummary {

  def row(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers.get(AllDocuments).map { attachments =>
      SummaryListRowViewModel(
        key = "uploadSupportingDocuments.checkYourAnswersLabel",
        value = ValueViewModel(
          HtmlContent(
            Html(
              attachments
                .map { attachment =>
                  attachment.file.fileName
                    .map(fileName => HtmlFormat.escape(fileName).body)
                    .getOrElse("")
                }
                .mkString("<br>")
            )
          )
        ),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.UploadAnotherSupportingDocumentController
              .onPageLoad(CheckMode, userAnswers.draftId)
              .url
          ).withVisuallyHiddenText(messages("doYouWantToUploadDocuments.files.change.hidden"))
        )
      )
    }
}
