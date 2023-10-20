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

package viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import models.requests.{Application, WhatIsYourRole}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RoleDetailsSummary {
  def rowRoleDescription(
    application: Application
  )(implicit messages: Messages): Option[SummaryListRow] =
    application.whatIsYourRoleResponse match {
      case Some(WhatIsYourRole.AgentOrg)    =>
        Some(
          SummaryListRowViewModel(
            key = "checkYourAnswersForAgents.applicant.role.label",
            value = ValueViewModel(messages("whatIsYourRoleAsImporter.agentOnBehalfOfOrg"))
          )
        )
      case Some(WhatIsYourRole.EmployeeOrg) =>
        Some(
          SummaryListRowViewModel(
            key = "checkYourAnswersForAgents.applicant.role.label",
            value = ValueViewModel(messages("whatIsYourRoleAsImporter.employeeOfOrg"))
          )
        )
      case Some(WhatIsYourRole.AgentTrader) =>
        Some(
          SummaryListRowViewModel(
            key = "checkYourAnswersForAgents.applicant.role.label",
            value = ValueViewModel(messages("whatIsYourRoleAsImporter.agentOnBehalfOfTrader"))
          )
        )
      case _                                => None
    }
}
