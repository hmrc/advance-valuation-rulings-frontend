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

import base.SpecBase
import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.IsSaleBetweenRelatedPartiesPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class IsSaleBetweenRelatedPartiesSummarySpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  ".row" - {

    Seq(
      (true, "site.yes"),
      (false, "site.no")
    ).foreach { case (answer, value) =>
      s"must create row for IsSaleBetweenRelatedPartiesSummary when answer is $answer" in {

        val userAnswers: UserAnswers = userAnswersAsIndividualTrader
          .set(IsSaleBetweenRelatedPartiesPage, answer)
          .success
          .value

        IsSaleBetweenRelatedPartiesSummary.row(userAnswers) mustBe Some(
          SummaryListRowViewModel(
            key = "isSaleBetweenRelatedParties.checkYourAnswersLabel",
            value = ValueViewModel(value),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.IsSaleBetweenRelatedPartiesController
                  .onPageLoad(CheckMode, userAnswers.draftId)
                  .url
              )
                .withVisuallyHiddenText(messages("isSaleBetweenRelatedParties.change.hidden"))
            )
          )
        )
      }
    }
  }
}
