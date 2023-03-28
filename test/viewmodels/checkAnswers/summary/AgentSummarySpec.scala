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

import scala.util.Try

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

import base.SpecBase
import models.{BusinessContactDetails, UserAnswers, WhatIsYourRoleAsImporter}
import pages.{BusinessContactDetailsPage, WhatIsYourRoleAsImporterPage}

class AgentSummarySpec extends SpecBase {

  private val ContactCompany = "Test Agent Company"

  val answers: Try[UserAnswers] =
    emptyUserAnswers
      .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.Agentonbehalfoforg)
      .flatMap(
        _.set(
          BusinessContactDetailsPage,
          BusinessContactDetails(ContactName, ContactEmail, ContactPhoneNumber, ContactCompany)
        )
      )

  "AgentSummary" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given empty user answers" - {
      val summary = AgentSummary(emptyUserAnswers)
      val rows    = summary.rows.rows

      "must create no rows" in {
        rows mustBe empty
      }
    }

    "when the user has answers for all relevant pages" - {
      val summary = AgentSummary(answers.success.value)
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "must create rows for each page" in {
        rows.length mustBe 5
      }

      "create row for business applicant name" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.applicant.name.label")),
            Value(Text(ContactName))
          )
        )
      }

      "create row for business applicant email" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.applicant.email.label")),
            Value(Text(ContactEmail))
          )
        )
      }

      "create row for business applicant company name" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.applicant.companyName.label")),
            Value(Text(ContactCompany))
          )
        )
      }

      "create row for business applicant role" in {
        rows must contain(
          (
            Key(Text("checkYourAnswersForAgents.applicant.role.label")),
            Value(
              Text(
                s"${WhatIsYourRoleAsImporter.MessagePrefix}.${WhatIsYourRoleAsImporter.Agentonbehalfoforg}"
              )
            )
          )
        )
      }
    }
  }
}
