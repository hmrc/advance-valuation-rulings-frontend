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
import models._
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.implicits._

import scala.util.Try

class MethodSummarySpec extends MethodSummaryFixtureSpec {

  "MethodSummary should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given empty user answers" - {
      val summary: MethodSummary = MethodSummary(userAnswersAsIndividualTrader)
      val rows                   = summary.rows.rows

      "must create no rows" in {
        rows mustBe empty
      }
    }

    "when the user has answers for all relevant pages for method one" - {
      val summary: MethodSummary = MethodSummary(methodOneAllAnswersInput.success.value)
      val rows                   = summary.rows.rows
      val keys                   = rows.map(_.key)

      "create details rows for all relavent pages" in {
        rows.length mustBe 7
      }

      "create row for valuation method" in {
        val key: Key = "valuationMethod.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for there a sale involved" in {
        val key: Key = "isThereASaleInvolved.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for sale between related parties" in {
        val key: Key = "isSaleBetweenRelatedParties.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for are there any restrictions on the goods" in {
        val key: Key = "areThereRestrictionsOnTheGoods.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for describe the restrictions" in {
        val key: Key = "describeTheRestrictions.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for is the sale subject to conditions" in {
        val key: Key = "isTheSaleSubjectToConditions.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for describe the conditions" in {
        val key: Key = "describeTheConditions.checkYourAnswersLabel"
        keys must contain(key)
      }
    }

    "when the user has answers method one without conditions or restrictions" - {
      val summary: MethodSummary = MethodSummary(methodOneShortFlow.success.value)
      val rows                   = summary.rows.rows
      val keys                   = rows.map(_.key)

      "create details rows for all relavent pages" in {
        rows.length mustBe 5
      }

      "create row for valuation method" in {
        val key: Key = "valuationMethod.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for there a sale involved" in {
        val key: Key = "isThereASaleInvolved.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for sale between related parties" in {
        val key: Key = "isSaleBetweenRelatedParties.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for are there any restrictions on the goods" in {
        val key: Key = "areThereRestrictionsOnTheGoods.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for is the sale subject to conditions" in {
        val key: Key = "isTheSaleSubjectToConditions.checkYourAnswersLabel"
        keys must contain(key)
      }
    }

    "when the user has answers for method two using method one in the past" - {
      val summary: MethodSummary = MethodSummary(methodTwoUsedMethodOneInPast.success.value)
      val rows                   = summary.rows.rows
      val keys                   = rows.map(_.key)

      "create details rows for all relavent pages" in {
        rows.length mustBe 4
      }

      "create row for valuation method" in {
        val key: Key = "valuationMethod.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for why identical goods" in {
        val key: Key = "whyIdenticalGoods.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for have you used method one in past" in {
        val key: Key = "haveYouUsedMethodOneInPast.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for describe the identical goods" in {
        val key: Key = "describeTheIdenticalGoods.checkYourAnswersLabel"
        keys must contain(key)
      }
    }

    "when the user has answers for all relevant pages for method three" - {
      val summary: MethodSummary = MethodSummary(methodThreeAllAnswersInput.success.value)
      val rows                   = summary.rows.rows
      val keys                   = rows.map(_.key)

      "create details rows for all relavent pages" in {
        rows.length mustBe 4
      }

      "create row for valuation method" in {
        val key: Key = "valuationMethod.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for why transaction value of similar goods" in {
        val key: Key = "whyTransactionValueOfSimilarGoods.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for have you used method one for similar goods in past" in {
        val key: Key = "haveYouUsedMethodOneForSimilarGoodsInPast.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for describe similar goods" in {
        val key: Key = "describeTheSimilarGoods.checkYourAnswersLabel"
        keys must contain(key)
      }
    }

    "when the user has answers for all relevant pages for method four" - {
      val summary: MethodSummary = MethodSummary(methodFourAllAnswersInput.success.value)
      val rows                   = summary.rows.rows
      val keys                   = rows.map(_.key)

      "create details rows for all relavent pages" in {
        rows.length mustBe 3
      }

      "create row for valuation method" in {
        val key: Key = "valuationMethod.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for explain why you have not selected method 1-3" in {
        val key: Key = "explainWhyYouHaveNotSelectedMethodOneToThree.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for explain why you chose method four" in {
        val key: Key = "explainWhyYouChoseMethodFour.checkYourAnswersLabel"
        keys must contain(key)
      }
    }

    "when the user has answers for all relevant pages for method five" - {
      val summary: MethodSummary = MethodSummary(methodFiveAllAnswersInput.success.value)
      val rows                   = summary.rows.rows
      val keys                   = rows.map(_.key)

      "create details rows for all relavent pages" in {
        rows.length mustBe 3
      }

      "create row for valuation method" in {
        val key: Key = "valuationMethod.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for why computed value" in {
        val key: Key = "whyComputedValue.checkYourAnswersLabel"
        keys must contain(key)
      }

      "create row for explain reason computed value" in {
        val key: Key = "explainReasonComputedValue.checkYourAnswersLabel"
        keys must contain(key)
      }

      "when the user has answers for all relevant pages for method six" - {
        val summary: MethodSummary = MethodSummary(methodSixAllAnswersInput.success.value)
        val rows                   = summary.rows.rows
        val keys                   = rows.map(_.key)

        "create details rows for all relavent pages" in {
          rows.length mustBe 4
        }

        "create row for valuation method" in {
          val key: Key = "valuationMethod.checkYourAnswersLabel"
          keys must contain(key)
        }

        "create row for explain why you have not selected method one to five" in {
          val key: Key = "explainWhyYouHaveNotSelectedMethodOneToFive.checkYourAnswersLabel"
          keys must contain(key)
        }

        "create row for adapt method" in {
          val key: Key = "adaptMethod.checkYourAnswersLabel"
          keys must contain(key)
        }

        "create row for explain how you will use method six" in {
          val key: Key = "explainHowYouWillUseMethodSix.checkYourAnswersLabel"
          keys must contain(key)
        }
      }
    }
  }
}

trait MethodSummaryFixtureSpec extends SpecBase {

  val methodOneAllAnswersInput: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method1)
      ua <- ua.set(IsThereASaleInvolvedPage, true)
      ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
      ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
      ua <- ua.set(DescribeTheRestrictionsPage, "test")
      ua <- ua.set(IsTheSaleSubjectToConditionsPage, true)
      ua <- ua.set(DescribeTheConditionsPage, "test")
    } yield ua

  val methodOneShortFlow: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method1)
      ua <- ua.set(IsThereASaleInvolvedPage, true)
      ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
      ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, false)
      ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
      // The following should not show up in the summary
      ua <- ua.set(DescribeTheRestrictionsPage, "test")
      ua <- ua.set(DescribeTheConditionsPage, "test")
    } yield ua

  val methodTwoUsedMethodOneInPast: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method2)
      ua <- ua.set(WhyIdenticalGoodsPage, "test")
      ua <- ua.set(HaveYouUsedMethodOneInPastPage, true)
      ua <- ua.set(DescribeTheIdenticalGoodsPage, "test")
    } yield ua

  val methodThreeAllAnswersInput: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method3)
      ua <- ua.set(WhyTransactionValueOfSimilarGoodsPage, "test")
      ua <- ua.set(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
      ua <- ua.set(DescribeTheSimilarGoodsPage, "test")
    } yield ua

  val methodFourAllAnswersInput: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method4)
      ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, "test")
      ua <- ua.set(ExplainWhyYouChoseMethodFourPage, "test")
    } yield ua

  val methodFiveAllAnswersInput: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method5)
      ua <- ua.set(WhyComputedValuePage, "test")
      ua <- ua.set(ExplainReasonComputedValuePage, "test")
    } yield ua

  val methodSixAllAnswersInput: Try[UserAnswers] =
    for {
      ua <- userAnswersAsIndividualTrader.set(ValuationMethodPage, ValuationMethod.Method6)
      ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, "test")
      ua <- ua.set(AdaptMethodPage, AdaptMethod.NoOtherMethod)
      ua <- ua.set(ExplainHowYouWillUseMethodSixPage, "test")
    } yield ua
}
