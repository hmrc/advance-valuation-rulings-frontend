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

package models

import generators.Generators
import generators.UserAnswersGenerator
// import matchers.should.Matchers._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import matchers.should.Matchers._
import models.ValuationMethod._
import base.SpecBase

class UserAnswersSpec
    extends SpecBase
    with ScalaCheckPropertyChecks
    with UserAnswersGenerator
    with Generators {

  "updateValuationMethod" - {
    "must clear answers for question one when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method1).success.value
          val result =
            UserAnswers.updateValuationMethod(withMethod, Method2).success.value

          result.get(ValuationMethodPage) shouldEqual Some(Method2)
          result.get(IsThereASaleInvolvedPage) shouldEqual None
          result.get(IsSaleBetweenRelatedPartiesPage) shouldEqual None
          result.get(IsTheSaleSubjectToConditionsPage) shouldEqual None
          result.get(DescribeTheConditionsPage) shouldEqual None
          result.get(AreThereRestrictionsOnTheGoodsPage) shouldEqual None
          result.get(DescribeTheRestrictionsPage) shouldEqual None
      }
    }

    "must clear answers for question two when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method2).success.value
          val result =
            UserAnswers.updateValuationMethod(withMethod, Method3).success.value

          result.get(ValuationMethodPage) shouldEqual Some(Method3)
          result.get(WhyIdenticalGoodsPage) shouldEqual None
          result.get(HaveYouUsedMethodOneInPastPage) shouldEqual None
          result.get(DescribeTheIdenticalGoodsPage) shouldEqual None
          result.get(WillYouCompareGoodsToIdenticalGoodsPage) shouldEqual None
          result.get(ExplainYourGoodsComparingToIdenticalGoodsPage) shouldEqual None
      }
    }

    "must clear answers for question three when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method3).success.value
          val result =
            UserAnswers.updateValuationMethod(withMethod, Method4).success.value

                      
          result.get(ValuationMethodPage) shouldEqual Some(Method4)
          result.get(WhyTransactionValueOfSimilarGoodsPage) shouldEqual None
          result.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) shouldEqual None
          result.get(WillYouCompareToSimilarGoodsPage) shouldEqual None
          result.get(ExplainYourGoodsComparingToSimilarGoodsPage) shouldEqual None
          result.get(DescribeTheSimilarGoodsPage) shouldEqual None
      }
    }

    "must clear answers for question four when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method4).success.value
          val result =
            UserAnswers.updateValuationMethod(withMethod, Method5).success.value

          result.get(ValuationMethodPage) shouldEqual Some(Method5)
          result.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) shouldEqual None
          result.get(ExplainWhyYouChoseMethodFourPage) shouldEqual None
      }
    }

    "must clear answers for question five when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method5).success.value
          val result =
            UserAnswers.updateValuationMethod(withMethod, Method6).success.value

          result.get(WhyComputedValuePage) shouldEqual None
          result.get(ExplainReasonComputedValuePage) shouldEqual None
          result.get(ValuationMethodPage) shouldEqual Some(Method6)
      }
    }

    "must clear answers for question six when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method6).success.value
          val result =
            UserAnswers.updateValuationMethod(withMethod, Method1).success.value

          result.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) shouldEqual None
          result.get(AdaptMethodPage) shouldEqual None
          result.get(ExplainHowYouWillUseMethodSixPage) shouldEqual None
      }
    }
  }
}
