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

class ApplicantSummarySpec extends SpecBase with Generators {
  import ApplicantSummarySpec._

  "ApplicantSummary should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    val summary: ApplicantSummary = ApplicantSummary(allAnswersInput.success.value)
    val rows                      = summary.rows.rows
    val keys                      = rows.map(_.key)
    "create details rows for all relavent pages" in {
      rows.length mustBe 6
    }

    "create details row for EORI number" in {
      val key: Key = "checkYourAnswers.eori.number.label"
      keys must contain(key)
    }

    "create details row for EORI registered name" in {
      val key: Key ="checkYourAnswers.eori.name.label"
      keys must contain(key)
    }

    "create details row for EORI registered address" in {
      val key: Key = "checkYourAnswers.eori.address.label"
      keys must contain(key)
    }

    "create details row for applicant name" in {
      val key: Key = "checkYourAnswers.applicant.name.label"
      keys must contain(key)
    }

    "create details for for applicant email" in {
      val key: Key = "checkYourAnswers.applicant.email.label"
      keys must contain(key)
    }

    "create details for for applicant phone" in {
      val key: Key = "checkYourAnswers.applicant.phone.label"
      keys must contain(key)
    }
  }
}

object ApplicantSummarySpec {
  val emptyUserAnswers = UserAnswers("test")

  val allAnswersInput: Try[UserAnswers] =
    emptyUserAnswers
      .set(DescriptionOfGoodsPage, "test")
      .flatMap(_.set(CheckRegisteredDetailsPage, CheckRegisteredDetails.Yes))
      .flatMap(
        _.set(
          ApplicationContactDetailsPage,
          ApplicationContactDetails(name = "test", email = "test", phone = "01234567890")
        )
      )
}
