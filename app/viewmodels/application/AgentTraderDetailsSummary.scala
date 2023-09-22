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

package viewmodels.application

import models.requests.ContactDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentTraderDetailsSummary {

  def rows(agent: ContactDetails)(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(
      SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.agent.org.name.label",
        value = ValueViewModel(agent.name)
      )
    ),
    Some(
      SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.agent.org.email.label",
        value = ValueViewModel(agent.email)
      )
    ),
    agent.phone.map {
      phone =>
        SummaryListRowViewModel(
          key = "checkYourAnswersForAgents.agent.org.phone.label",
          value = ValueViewModel(phone)
        )
    },
    agent.companyName.map {
      companyName =>
        SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.applicant.companyName.label",
        value = ValueViewModel(companyName)
      )
    },
    Some(
      SummaryListRowViewModel(
        key = "checkYourAnswersForAgents.applicant.role.label",
        value = ValueViewModel(messages("whatIsYourRoleAsImporter.agentOnBehalfOfTrader"))
      )
    ),
  ).flatten
}
