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

package viewmodels.checkAnswers.summary

import base.SpecBase
import generators.Generators
import models.UserAnswers
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.implicits._

import scala.util.Try

class DetailsSummarySpec extends DetailsSummaryFixtureSpec {

  "DetailsSummary should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    val summary: DetailsSummary = DetailsSummary(allAnswersInput.success.value)
    val rows                    = summary.rows.rows
    val keys                    = rows.map(_.key)

    "when given empty user answers" - {
      val summary: DetailsSummary = DetailsSummary(userAnswersAsIndividualTrader)
      val rows                    = summary.rows.rows

      "must create no rows" in {
        rows mustBe empty
      }
    }

    "when the user has answers for all relevant pages" - {
      "create rows for each pages" in {
        rows.length mustBe 8
      }

      "create row for description of goods" in {
        val key: Key = "descriptionOfGoods.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for has commidity code" in {
        val key: Key = "hasCommodityCode.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for commodityCode code" in {
        val key: Key = "commodityCode.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for has legal challenges" in {
        val expected = "haveTheGoodsBeenSubjectToLegalChallenges.checkYourAnswersLabel"
        val key: Key = expected
        keys must contain(key)
      }

      "create row for legal challenges" in {
        val key: Key = "describeTheLegalChallenges.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for has confidential information" in {
        val key: Key = "hasConfidentialInformation.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for confidential information" in {
        val key: Key = "confidentialInformation.checkYourAnswersLabel"
        keys must contain(key)
      }
    }

  }
}

trait DetailsSummaryFixtureSpec extends SpecBase with Generators {

  val allAnswersInput: Try[UserAnswers] =
    userAnswersAsIndividualTrader
      .set(DescriptionOfGoodsPage, "test")
      .flatMap(_.set(HasCommodityCodePage, true))
      .flatMap(_.set(CommodityCodePage, "test"))
      .flatMap(_.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, true))
      .flatMap(_.set(DescribeTheLegalChallengesPage, "test"))
      .flatMap(_.set(HasConfidentialInformationPage, true))
      .flatMap(_.set(ConfidentialInformationPage, "test"))
      .flatMap(_.set(DoYouWantToUploadDocumentsPage, true))
}
