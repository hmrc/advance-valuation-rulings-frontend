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
import models.{CheckMode, UserAnswers}
import pages.ExplainHowPartiesAreRelatedPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class ExplainHowPartiesAreRelatedSummarySpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()
  private val userAnswers: UserAnswers    = userAnswersAsIndividualTrader
    .set(ExplainHowPartiesAreRelatedPage, "test string")
    .success
    .value

  ".row" - {

    "must create row for ExplainHowPartiesAreRelatedSummary" in {

      ExplainHowPartiesAreRelatedSummary.row(userAnswers) mustBe Some(
        SummaryListRowViewModel(
          key = "explainHowPartiesAreRelated.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent("test string".replace("\n", "<br/>"))),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.ExplainHowPartiesAreRelatedController
                .onPageLoad(CheckMode, userAnswers.draftId)
                .url
            )
              .withVisuallyHiddenText(messages("explainHowPartiesAreRelated.change.hidden"))
          )
        )
      )
    }
  }
}