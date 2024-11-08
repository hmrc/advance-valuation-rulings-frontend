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

import base.SpecBase
import controllers.routes
import models._
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.{Html, HtmlFormat}
import queries.AllDocuments
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.Instant

class UploadedDocumentsSummarySpec extends SpecBase {

  private implicit val messages: Messages             = stubMessages()
  private val file: UploadedFile.Success              = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now,
      checksum = "checksum",
      size = 1337
    )
  )
  private val draftAttachments: List[DraftAttachment] = List(DraftAttachment(file, Some(true)))
  private val userAnswers: UserAnswers                = userAnswersAsIndividualTrader
    .set(AllDocuments, draftAttachments)
    .success
    .value

  ".row" - {

    "must create row for UploadedDocumentsSummary" in {

      UploadedDocumentsSummary.row(userAnswers) mustBe Some(
        SummaryListRowViewModel(
          key = "uploadSupportingDocuments.checkYourAnswersLabel",
          value = ValueViewModel(
            HtmlContent(
              Html(
                draftAttachments
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
      )
    }
  }
}
