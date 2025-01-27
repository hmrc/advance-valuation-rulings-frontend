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

import models.Country
import models.requests.TraderDetail
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentDetailsSummary {

  def rows(agent: TraderDetail)(implicit messages: Messages): Seq[SummaryListRow] = {

    val country = Country.fromCountryCode(agent.countryCode)

    val addressLines = Seq(
      Some(agent.addressLine1),
      agent.addressLine2,
      agent.addressLine3,
      Some(agent.postcode),
      Some(country.name)
    ).flatten.mkString("<br/>")

    Seq(
      SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.agent.eori.number.label",
        value = ValueViewModel(agent.eori)
      ),
      SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.agent.name.label",
        value = ValueViewModel(agent.businessName)
      ),
      SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.agent.address.label",
        value = ValueViewModel(HtmlContent(Html(addressLines)))
      )
    )
  }
}
