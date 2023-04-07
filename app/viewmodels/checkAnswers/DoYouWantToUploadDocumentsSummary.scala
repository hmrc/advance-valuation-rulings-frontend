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

import cats.syntax.all._

import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models._
import models.requests.Application
import pages.{DoYouWantToUploadDocumentsPage, UploadSupportingDocumentPage}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DoYouWantToUploadDocumentsSummary {

  private def makeRow(answer: Boolean)(implicit messages: Messages) = {

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

  def row(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers
      .get(DoYouWantToUploadDocumentsPage)
      .map(makeRow)

  def row(application: Application)(implicit messages: Messages): Option[SummaryListRow] =
    makeRow(application.attachments.nonEmpty).some
}

object UploadedDocumentsSummary {

  private def makeRow(fileNames: Seq[String])(implicit messages: Messages) =
    if (fileNames.isEmpty) {
      None
    } else {
      Some(
        SummaryListRowViewModel(
          key = "uploadSupportingDocuments.checkYourAnswersLabel",
          value = ValueViewModel(
            HtmlContent(
              Html(
                fileNames.map(fileName => Html(s"${HtmlFormat.escape(fileName).body}<br>")).mkString
              )
            )
          ),
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

  def row(userAnswers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    userAnswers
      .get(UploadSupportingDocumentPage)
      .map(_.files.map(_._2.fileName).toSeq)
      .flatMap(makeRow)

  def row(application: Application)(implicit messages: Messages): Option[SummaryListRow] =
    makeRow(application.attachments.map(_.name))
}
