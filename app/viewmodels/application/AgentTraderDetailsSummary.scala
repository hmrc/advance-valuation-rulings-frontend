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

package viewmodels.application

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import models.requests.{Attachment, ContactDetails, TraderDetail}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentTraderDetailsSummary extends ApplicationSummaryHelper {

  def rowLetterOfAuthority(
    letterOfAuthority: Option[Attachment]
  )(implicit messages: Messages): Option[SummaryListRow] =
    letterOfAuthority match {
      case Some(value) =>
        Some(
          SummaryListRowViewModel(
            key = "agentForTraderCheckYourAnswers.trader.loa.label",
            value = ValueViewModel(value.name)
          )
        )
      case _           => None
    }

  def rowTraderEori(eori: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = "agentForTraderCheckYourAnswers.trader.eori.number.label",
      value = ValueViewModel(eori)
    )

  def rowsTraderDetails(traderDetail: TraderDetail)(implicit
    messages: Messages
  ): Seq[SummaryListRow] = {
    val addressLines = Seq(
      Some(traderDetail.addressLine1),
      traderDetail.addressLine2,
      traderDetail.addressLine3,
      Some(traderDetail.postcode),
      Some(traderDetail.countryCode)
    ).flatten.mkString("<br/>")

    Seq(
      Some(
        SummaryListRowViewModel(
          key = "agentForTraderCheckYourAnswers.trader.name.label",
          value = ValueViewModel(traderDetail.businessName)
        )
      ),
      Some(
        SummaryListRowViewModel(
          key = "agentForTraderCheckYourAnswers.trader.address.label",
          value = ValueViewModel(HtmlContent(Html(addressLines)))
        )
      )
    ).flatten
  }

  def rowsAgentDetails(contact: ContactDetails)(implicit
    messages: Messages
  ): Seq[SummaryListRow] = {
    val mandatoryRows = Seq(
      SummaryListRowViewModel(
        key = "agentForTraderCheckYourAnswers.applicant.name.label",
        value = ValueViewModel(contact.name)
      ),
      SummaryListRowViewModel(
        key = "agentForTraderCheckYourAnswers.applicant.email.label",
        value = ValueViewModel(contact.email)
      )
    )

    val phoneRow = makeRowFromOption(
      key = "agentForTraderCheckYourAnswers.applicant.phone.label",
      field = contact.phone
    )

    val companyNameRow = makeRowFromOption(
      key = "agentForTraderCheckYourAnswers.applicant.companyName.label",
      field = contact.companyName
    )

    val jobTitleRow = makeRowFromOption(
      key = "agentForTraderCheckYourAnswers.applicant.jobTitle.label",
      field = contact.jobTitle
    )

    mandatoryRows ++ phoneRow ++ companyNameRow ++ jobTitleRow
  }

}
