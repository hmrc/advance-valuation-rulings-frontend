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
import models.UserAnswers
import pages._
import viewmodels.implicits._

class DetailsSummarySpec extends SpecBase with Generators {
  import DetailsSummarySpec._

  "DetailsSummary should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    val summary: DetailsSummary = DetailsSummary(allAnswersInput.success.value)
    val rows                    = summary.rows.rows
    val keys                    = rows.map(_.key)

    "create details rows for all relavent pages" in {
      rows.length mustBe 8
    }

    "does not include empty rows" in {
      DetailsSummary(emptyUserAnswers).rows.rows mustBe empty
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

object DetailsSummarySpec {
  val emptyUserAnswers = UserAnswers("test")

  val allAnswersInput: Try[UserAnswers] =
    emptyUserAnswers
      .set(DescriptionOfGoodsPage, "test")
      .flatMap(_.set(HasCommodityCodePage, true))
      .flatMap(_.set(CommodityCodePage, "test"))
      .flatMap(_.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, true))
      .flatMap(_.set(DescribeTheLegalChallengesPage, "test"))
      .flatMap(_.set(HasConfidentialInformationPage, true))
      .flatMap(_.set(ConfidentialInformationPage, "test"))
      .flatMap(_.set(DoYouWantToUploadDocumentsPage, true))
}
