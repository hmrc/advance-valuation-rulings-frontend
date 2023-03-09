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

import base.SpecBase
import generators.Generators
import generators.ModelGenerators
import generators.UserAnswersGenerator
import models._
import models.ValuationMethod._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class UserAnswersSpec
    extends SpecBase
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with UserAnswersGenerator
    with Generators {

  "updateValuationMethod" - {
    "must clear answers for question one when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method1).success.value
          val result     =
            UserAnswers.updateValuationMethod(withMethod, Method2).success.value

          result.get(ValuationMethodPage)                must be(Some(Method2))
          result.get(IsThereASaleInvolvedPage)           must be(empty)
          result.get(IsSaleBetweenRelatedPartiesPage)    must be(empty)
          result.get(IsTheSaleSubjectToConditionsPage)   must be(empty)
          result.get(DescribeTheConditionsPage)          must be(empty)
          result.get(AreThereRestrictionsOnTheGoodsPage) must be(empty)
          result.get(DescribeTheRestrictionsPage)        must be(empty)
      }
    }

    "must clear answers for question two when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method2).success.value
          val result     =
            UserAnswers.updateValuationMethod(withMethod, Method3).success.value

          result.get(ValuationMethodPage)                           must be(Some(Method3))
          result.get(WhyIdenticalGoodsPage)                         must be(empty)
          result.get(HaveYouUsedMethodOneInPastPage)                must be(empty)
          result.get(DescribeTheIdenticalGoodsPage)                 must be(empty)
          result.get(WillYouCompareGoodsToIdenticalGoodsPage)       must be(empty)
          result.get(ExplainYourGoodsComparingToIdenticalGoodsPage) must be(empty)
      }
    }

    "must clear answers for question three when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method3).success.value
          val result     =
            UserAnswers.updateValuationMethod(withMethod, Method4).success.value

          result.get(ValuationMethodPage)                           must be(Some(Method4))
          result.get(WhyTransactionValueOfSimilarGoodsPage)         must be(empty)
          result.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) must be(empty)
          result.get(WillYouCompareToSimilarGoodsPage)              must be(empty)
          result.get(ExplainYourGoodsComparingToSimilarGoodsPage)   must be(empty)
          result.get(DescribeTheSimilarGoodsPage)                   must be(empty)
      }
    }

    "must clear answers for question four when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method4).success.value
          val result     =
            UserAnswers.updateValuationMethod(withMethod, Method5).success.value

          result.get(ValuationMethodPage)                              must be(Some(Method5))
          result.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) must be(empty)
          result.get(ExplainWhyYouChoseMethodFourPage)                 must be(empty)
      }
    }

    "must clear answers for question five when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method5).success.value
          val result     =
            UserAnswers.updateValuationMethod(withMethod, Method6).success.value

          result.get(WhyComputedValuePage)           must be(empty)
          result.get(ExplainReasonComputedValuePage) must be(empty)
          result.get(ValuationMethodPage)            must be(Some(Method6))
      }
    }

    "must clear answers for question six when it was the last method used" in {
      val answers = arbitrary[UserAnswers]

      forAll(answers) {
        answers =>
          val withMethod = answers.set(ValuationMethodPage, Method6).success.value
          val result     =
            UserAnswers.updateValuationMethod(withMethod, Method1).success.value

          result.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) must be(empty)
          result.get(AdaptMethodPage)                                 must be(empty)
          result.get(ExplainHowYouWillUseMethodSixPage)               must be(empty)
          result.get(ValuationMethodPage)                             must be(Some(Method1))
      }
    }
  }
}
