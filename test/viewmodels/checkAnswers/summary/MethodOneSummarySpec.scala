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
import models._
import pages._
import viewmodels.implicits._

class MethodOneSummarySpec extends SpecBase {
  import MethodOneSummarySpec._

  "MethodOneSummary should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    val summary: MethodOneSummary = MethodOneSummary(allAnswersInput.success.value)
    val rows                      = summary.rows.rows
    val keys                      = rows.map(_.key)
    "create details rows for all relavent pages" in {
      rows.length mustBe 6
    }

    "does not include empty rows" in {
      MethodOneSummary(emptyUserAnswers).rows.rows mustBe empty
    }

    "create details row is there a sale involved" in {
      val key: Key = "isThereASaleInvolved.checkYourAnswersLabel"
      keys must contain(key)
    }

    "create details row is sale between related parties" in {
      val key: Key = "isSaleBetweenRelatedParties.checkYourAnswersLabel"
      keys must contain(key)
    }

    "create details row for are there any restrictions on the goods" in {
      val key: Key = "areThereRestrictionsOnTheGoods.checkYourAnswersLabel"
      keys must contain(key)
    }

    "create details row for describe the restrictions" in {
      val key: Key = "describeTheRestrictions.checkYourAnswersLabel"
      keys must contain(key)
    }

    "create details row for is the sale subject to conditions" in {
      val key: Key = "isTheSaleSubjectToConditions.checkYourAnswersLabel"
      keys must contain(key)
    }

    "create details row for describe the conditions" in {
      val key: Key = "describeTheConditions.checkYourAnswersLabel"
      keys must contain(key)
    }
  }
}

object MethodOneSummarySpec {
  val emptyUserAnswers = UserAnswers("test")

  val allAnswersInput: Try[UserAnswers] =
    for {
      ua <- emptyUserAnswers.set(IsThereASaleInvolvedPage, true)
      ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
      ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
      ua <- ua.set(DescribeTheRestrictionsPage, "test")
      ua <- ua.set(IsTheSaleSubjectToConditionsPage, true)
      ua <- ua.set(DescribeTheConditionsPage, "test")
    } yield ua
}
