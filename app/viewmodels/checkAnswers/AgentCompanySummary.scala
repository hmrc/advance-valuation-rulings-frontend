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
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models.{AgentCompanyDetails, CheckMode, DraftId, UserAnswers}
import models.requests.ApplicationRequest
import pages.AgentCompanyDetailsPage
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentCompanySummary {

  private def registeredNameRow(answer: AgentCompanyDetails, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.business.name.label",
      value = ValueViewModel(HtmlFormat.escape(answer.agentCompanyName).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.AgentCompanyDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.change.hidden"))
      )
    )

  private def registeredAddressRow(answer: AgentCompanyDetails, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.business.address.label",
      value = ValueViewModel(
        HtmlContent(
          Html(
            s"${HtmlFormat.escape(answer.agentStreetAndNumber).body}<br>" +
              s"${HtmlFormat.escape(answer.agentCity).body}<br>" +
              answer.agentPostalCode
                .map(value => s"${HtmlFormat.escape(value).body}<br>")
                .getOrElse("") +
              s"${HtmlFormat.escape(answer.agentCountry).body}"
          )
        )
      ),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.AgentCompanyDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.change.hidden"))
      )
    )

  private def registeredEoriNumberRow(answer: AgentCompanyDetails, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.business.eori.number.label",
      value = ValueViewModel(HtmlFormat.escape(answer.agentEori).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.AgentCompanyDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.change.hidden"))
      )
    )

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(AgentCompanyDetailsPage)
      eori            = registeredEoriNumberRow(contactDetails, userAnswer.draftId)
      name            = registeredNameRow(contactDetails, userAnswer.draftId)
      address         = registeredAddressRow(contactDetails, userAnswer.draftId)
      result          = Seq(eori, name, address)
    } yield result

  def rows(
    request: ApplicationRequest
  )(implicit messages: Messages): Seq[SummaryListRow] = {
    val postCode       =
      if (request.trader.postcode.isEmpty) None else Some(request.trader.postcode)
    val contactDetails = models.AgentCompanyDetails(
      agentEori = request.trader.eori,
      agentCompanyName = request.trader.businessName,
      agentStreetAndNumber = request.trader.addressLine1 + "\n" + request.trader.addressLine2,
      agentCity = request.trader.addressLine2.getOrElse(""),
      agentCountry = request.trader.countryCode,
      agentPostalCode = postCode
    )
    Seq(
      registeredEoriNumberRow(contactDetails, DraftId(0)),
      registeredNameRow(contactDetails, DraftId(0)),
      registeredAddressRow(contactDetails, DraftId(0)) // TODO: NICK: Fix this!
    )
  }

}
