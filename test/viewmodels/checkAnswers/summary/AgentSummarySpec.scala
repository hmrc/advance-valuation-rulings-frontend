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

package viewmodels.checkAnswers.summary

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

import base.SpecBase
import models.{AgentCompanyDetails, BusinessContactDetails, Country, UserAnswers, WhatIsYourRoleAsImporter}
import pages.{AgentCompanyDetailsPage, BusinessContactDetailsPage, WhatIsYourRoleAsImporterPage}

class AgentSummarySpec extends SpecBase {

  val answers: UserAnswers =
    userAnswersAsIndividualTrader
      .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
      .success
      .value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails(ContactName, ContactEmail, ContactPhoneNumber, None)
      )
      .success
      .value
      .set(
        AgentCompanyDetailsPage,
        AgentCompanyDetails(
          agentEori = EoriNumber,
          agentCompanyName = RegisteredName,
          agentStreetAndNumber = StreetAndNumber,
          agentCity = City,
          agentCountry = Country("GB", "United Kingdom"),
          agentPostalCode = Some(Postcode)
        )
      )
      .success
      .value

  "AgentSummary" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given empty user answers" - {
      val summary = AgentSummary(userAnswersAsIndividualTrader)
      val rows    = summary.rows.rows

      "must create no rows" in {
        rows mustBe empty
      }
    }

    "when the user has answers for all relevant pages" - {
      val summary = AgentSummary(answers)
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "must create rows for each page" in {
        rows.length mustBe 6
      }

      "create row for agent applicant name" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.agent.org.name.label")),
            Value(Text(ContactName))
          )
        )
      }

      "create row for agent applicant email" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.agent.org.email.label")),
            Value(Text(ContactEmail))
          )
        )
      }

      "create row for agent EORI number" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.agent.eori.number.label")),
            Value(Text(EoriNumber))
          )
        )
      }

      "create row for agent registered name" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.agent.name.label")),
            Value(Text(RegisteredName))
          )
        )
      }

      "create row for agent registered address" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.agent.address.label")),
            Value(HtmlContent(s"$StreetAndNumber<br>$City<br>$Postcode<br>$country"))
          )
        )
      }
    }
  }
}
