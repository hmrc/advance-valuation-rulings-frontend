/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ExplainWhyYouHaveNotSelectedMethodOneToThreePage
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ExplainWhyYouHaveNotSelectedMethodOneToThreeSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage).map { answer =>
      SummaryListRowViewModel(
        key = "explainWhyYouHaveNotSelectedMethodOneToThree.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(answer.replace("\n", "<br/>"))),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.ExplainWhyYouHaveNotSelectedMethodOneToThreeController
              .onPageLoad(CheckMode, answers.draftId)
              .url
          )
            .withVisuallyHiddenText(
              messages("explainWhyYouHaveNotSelectedMethodOneToThree.change.hidden")
            )
        )
      )
    }
}
