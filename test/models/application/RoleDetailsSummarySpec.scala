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

package models.application

import base.SpecBase
import models.requests.WhatIsYourRole
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.application.RoleDetailsSummary

class RoleDetailsSummarySpec extends SpecBase {

  implicit val messages: Messages = messages(applicationBuilder().build())

  "RoleDetailsSummary object" - {
    "rowRoleDescription method" - {
      "should return the correct role description for AgentOrg" in {
        val result = RoleDetailsSummary.rowRoleDescription(Some(WhatIsYourRole.AgentOrg))

        result.get.key.content mustBe Text(messages("checkYourAnswersForAgents.applicant.role.label"))
        result.get.value.content mustBe Text(messages("whatIsYourRoleAsImporter.agentOnBehalfOfOrg"))
      }

      "should return the correct role description for EmployeeOrg" in {
        val result = RoleDetailsSummary.rowRoleDescription(Some(WhatIsYourRole.EmployeeOrg))

        result.get.key.content mustBe Text(messages("checkYourAnswersForAgents.applicant.role.label"))
        result.get.value.content mustBe Text(messages("whatIsYourRoleAsImporter.employeeOfOrg"))
      }

      "should return the correct role description for AgentTrader" in {
        val result = RoleDetailsSummary.rowRoleDescription(Some(WhatIsYourRole.AgentTrader))

        result.get.key.content mustBe Text(messages("checkYourAnswersForAgents.applicant.role.label"))
        result.get.value.content mustBe Text(messages("whatIsYourRoleAsImporter.agentOnBehalfOfTrader"))
      }

      "should return None for other roles" in {
        val result = RoleDetailsSummary.rowRoleDescription(None)

        result mustBe None
      }
    }
  }
}
