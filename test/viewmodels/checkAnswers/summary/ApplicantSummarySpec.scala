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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import base.SpecBase
import generators.Generators
import models._
import pages._
import viewmodels.implicits._

class ApplicantSummarySpec extends ApplicantSummaryFixtureSpec {

  "ApplicantSummary" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given empty user answers" - {
      val summary: ApplicantSummary = ApplicantSummary(emptyUserAnswers)
      val rows                      = summary.rows.rows

      "must create no rows" in {
        rows mustBe empty
      }
    }

    "when the user has answers for all relevant pages" - {

      val summary: ApplicantSummary = ApplicantSummary(allAnswersInput.success.value)
      val rows                      = summary.rows.rows
      val keys                      = rows.map(_.key)

      "must create rows for each page" in {
        rows.length mustBe 6
      }

      "create row for EORI number" in {
        val key: Key = "checkYourAnswers.eori.number.label"
        keys must contain(key)
      }

      "create row for EORI registered name" in {
        val key: Key = "checkYourAnswers.eori.name.label"
        keys must contain(key)
      }

      "create row for EORI registered address" in {
        val key: Key = "checkYourAnswers.eori.address.label"
        keys must contain(key)
      }

      "create row for applicant name" in {
        val key: Key = "checkYourAnswers.applicant.name.label"
        keys must contain(key)
      }

      "create row for applicant email" in {
        val key: Key = "checkYourAnswers.applicant.email.label"
        keys must contain(key)
      }

      "create row for applicant phone" in {
        val key: Key = "checkYourAnswers.applicant.phone.label"
        keys must contain(key)
      }
    }
  }
}

trait ApplicantSummaryFixtureSpec extends SpecBase with Generators {

  val allAnswersInput: Try[UserAnswers] =
    emptyUserAnswers
      .set(DescriptionOfGoodsPage, "test")
      .flatMap(
        _.set(
          CheckRegisteredDetailsPage,
          CheckRegisteredDetails(
            true,
            "eorinumber",
            "name",
            "streetAndNumber",
            "city",
            "country",
            Some("postcode")
          )
        )
      )
      .flatMap(
        _.set(
          ApplicationContactDetailsPage,
          ApplicationContactDetails(name = "test", email = "test", phone = "01234567890")
        )
      )
}
