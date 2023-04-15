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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AgentCompanyDetailsPage
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentCompanyDetailsSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AgentCompanyDetailsPage).map {
      answer =>
        val value = HtmlFormat.escape(answer.agentEori).toString +
          "<br/>" + HtmlFormat.escape(answer.agentCompanyName).toString +
          "<br/>" + HtmlFormat.escape(answer.agentStreetAndNumber).toString +
          "<br/>" + HtmlFormat.escape(answer.agentCity).toString +
          "<br/>" + HtmlFormat.escape(answer.agentCountry).toString +
          "<br/>" + HtmlFormat.escape(answer.agentPostalCode.getOrElse("")).toString

        SummaryListRowViewModel(
          key = "agentCompanyDetails.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.AgentCompanyDetailsController.onPageLoad(CheckMode).url
            )
              .withVisuallyHiddenText(messages("agentCompanyDetails.change.hidden"))
          )
        )
    }
}
