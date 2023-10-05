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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import base.SpecBase
import models._
import pages._

class IndividualApplicantSummarySpec extends SpecBase {

  val answers: Try[UserAnswers] =
    userAnswersAsIndividualTrader
      .set(
        ApplicationContactDetailsPage,
        ApplicationContactDetails(ContactName, ContactEmail, ContactPhoneNumber, JobTitle)
      )

  "IndividualApplicantSummary" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given empty user answers" - {
      val summary = IndividualApplicantSummary(userAnswersAsIndividualTrader)
      val rows    = summary.rows.rows

      "must create no rows" in {
        rows mustBe empty
      }
    }

    "when the user has answers for all relevant pages" - {
      val summary = IndividualApplicantSummary(answers.success.value)
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "must create rows for each page" in {
        rows.length mustBe 3
      }

      "create row for applicant name" in {
        rows must contain(
          (
            Key(Text("checkYourAnswers.applicant.name.label")),
            Value(Text(ContactName))
          )
        )
      }

      "create row for applicant email" in {
        rows must contain(
          (
            Key(Text("checkYourAnswers.applicant.email.label")),
            Value(Text(ContactEmail))
          )
        )
      }

      "create row for applicant phone" in {
        rows must contain(
          (
            Key(Text("checkYourAnswers.applicant.phone.label")),
            Value(Text(ContactPhoneNumber))
          )
        )
      }
    }
  }
}
