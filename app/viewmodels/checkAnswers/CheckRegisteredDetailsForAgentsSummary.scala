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

import controllers.routes
import models.{CheckMode, DraftId, EoriNumber, UserAnswers}
import pages.CheckRegisteredDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CheckRegisteredDetailsForAgentsSummary {

  private def registeredNameRow(answer: Boolean, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow = ???

  private def registeredAddressRow(answer: Boolean, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.business.address.label",
      value = ValueViewModel(
        // HtmlContent(
        // Html(
        // s"${HtmlFormat.escape(answer.streetAndNumber).body}<br>" +
        //   s"${HtmlFormat.escape(answer.city).body}<br>" +
        //   answer.postalCode
        //     .map(value => s"${HtmlFormat.escape(value).body}<br>")
        //     .getOrElse("") +
        //   s"${HtmlFormat.escape(answer.country).body}"
        ???
        // )
        // )
      ),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("checkYourAnswersForAgents.business.address.hidden"))
      )
    )

  private def registeredNumberRow(eoriNumber: EoriNumber, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.business.eori.number.label",
      value = ValueViewModel(HtmlFormat.escape(eoriNumber.value).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("checkYourAnswersForAgents.business.eori.number.hidden"))
      )
    )

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(CheckRegisteredDetailsPage)
      number          = registeredNumberRow(EoriNumber(???), userAnswer.draftId)
    } yield {
      val personalDetails = if (???) {
        val name    = registeredNameRow(contactDetails, userAnswer.draftId)
        val address = registeredAddressRow(contactDetails, userAnswer.draftId)
        Seq(name, address)
      } else {
        Nil
      }
      number +: personalDetails
    }
}
