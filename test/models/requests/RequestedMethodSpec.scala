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

package models.requests

import cats.data.NonEmptyList
import cats.data.Validated._

import generators._
import models._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class RequestedMethodSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import RequestedMethodSpec._

  "RequestedMethod" should {

    "return valid for method one - shortest path" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method1)
        ua <- ua.set(IsThereASaleInvolvedPage, true)
        ua <- ua.set(IsSaleBetweenRelatedPartiesPage, false)
        ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
        ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, false)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodOne(
          None,
          None,
          None
        )
      )
    }

    "return valid for method one - long path" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method1)
        ua <- ua.set(IsThereASaleInvolvedPage, true)
        ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
        ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
        ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
        ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
        ua <- ua.set(IsTheSaleSubjectToConditionsPage, true)
        ua <- ua.set(DescribeTheConditionsPage, "describeTheConditions")
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodOne(
          Some("explainHowPartiesAreRelated"),
          Some("describeTheRestrictions"),
          Some("describeTheConditions")
        )
      )
    }

    "return invalid for method one when there is no sale involved" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method1)
        ua <- ua.set(IsThereASaleInvolvedPage, false)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.of(
          IsThereASaleInvolvedPage
        )
      )
    }

    "return valid when explanations are missing" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method1)
        ua <- ua.set(IsThereASaleInvolvedPage, true)
        ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
        ua <- ua.set(IsTheSaleSubjectToConditionsPage, true)
        ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.of(
          ExplainHowPartiesAreRelatedPage,
          DescribeTheRestrictionsPage,
          DescribeTheConditionsPage
        )
      )
    }

    "return invalid for method one with no answers" in {
      val userAnswers =
        emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method1).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.of(
          IsThereASaleInvolvedPage
        )
      )
    }

    "return valid for method two when has used method one in past" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method2)
        ua <- ua.set(WhyIdenticalGoodsPage, randomString)
        ua <- ua.set(HaveYouUsedMethodOneInPastPage, true)
        ua <- ua.set(DescribeTheIdenticalGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodTwo(
          randomString,
          PreviousIdenticalGoods(randomString)
        )
      )
    }

    "return invalid when WhyIdenticalGoodsPage is not answered" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method2)
        ua <- ua.set(HaveYouUsedMethodOneInPastPage, true)
        ua <- ua.set(DescribeTheIdenticalGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(
          WhyIdenticalGoodsPage
        )
      )
    }

    "return invalid if has not used method one in pagt" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method2)
        ua <- ua.set(WhyIdenticalGoodsPage, randomString)
        ua <- ua.set(HaveYouUsedMethodOneInPastPage, false)
        ua <- ua.set(DescribeTheIdenticalGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(
          HaveYouUsedMethodOneInPastPage
        )
      )
    }

    "return invalid for method two with no answers" in {
      val userAnswers =
        emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method2).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.of(
          WhyIdenticalGoodsPage,
          HaveYouUsedMethodOneInPastPage,
          DescribeTheIdenticalGoodsPage
        )
      )
    }

    "return valid for method three with answers for all questions" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method3)
        ua <- ua.set(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
        ua <- ua.set(DescribeTheSimilarGoodsPage, randomString)
        ua <- ua.set(WhyTransactionValueOfSimilarGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodThree(
          randomString,
          PreviousSimilarGoods(randomString)
        )
      )
    }

    "return invalid for method three with comparing with similar goods set to false" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method3)
        ua <- ua.set(HaveYouUsedMethodOneForSimilarGoodsInPastPage, false)
        ua <- ua.set(DescribeTheSimilarGoodsPage, randomString)
        ua <- ua.set(WhyTransactionValueOfSimilarGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(HaveYouUsedMethodOneForSimilarGoodsInPastPage)
      )
    }

    "return invalid for method three without HaveYouUsedMethodOneForSimilarGoodsInPastPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method3)
        ua <- ua.set(DescribeTheSimilarGoodsPage, randomString)
        ua <- ua.set(WhyTransactionValueOfSimilarGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(HaveYouUsedMethodOneForSimilarGoodsInPastPage)
      )
    }

    "return invalid for method three without DescribeTheSimilarGoodsPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method3)
        ua <- ua.set(HaveYouUsedMethodOneForSimilarGoodsInPastPage, true)
        ua <- ua.set(WhyTransactionValueOfSimilarGoodsPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(DescribeTheSimilarGoodsPage)
      )
    }

    "return invalid for method three with no answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method3)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.of(
          WhyTransactionValueOfSimilarGoodsPage,
          HaveYouUsedMethodOneForSimilarGoodsInPastPage,
          DescribeTheSimilarGoodsPage
        )
      )
    }

    "return valid for method four with all answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, randomString)
        ua <- ua.set(ExplainWhyYouChoseMethodFourPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodFour(
          randomString,
          randomString
        )
      )
    }

    "return invalid for method four without ExplainWhyYouHaveNotSelectedMethodOneToThreePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
        ua <- ua.set(ExplainWhyYouChoseMethodFourPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainWhyYouHaveNotSelectedMethodOneToThreePage)
      )
    }

    "return invalid for method four without ExplainWhyYouChoseMethodFourPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainWhyYouChoseMethodFourPage)
      )
    }

    "return invalid for method four with only ValuationMethodPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
          List(ExplainWhyYouChoseMethodFourPage)
        )
      )
    }

    "return valid for method five with all answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
        ua <- ua.set(WhyComputedValuePage, randomString)
        ua <- ua.set(ExplainReasonComputedValuePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodFive(
          randomString,
          randomString
        )
      )
    }

    "return invalid for method five without WhyComputedValuePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
        ua <- ua.set(ExplainReasonComputedValuePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(WhyComputedValuePage)
      )
    }

    "return invalid for method five without ExplainReasonComputedValuePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
        ua <- ua.set(WhyComputedValuePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainReasonComputedValuePage)
      )
    }

    "return invalid for method five with only ValuationMethodPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          WhyComputedValuePage,
          List(ExplainReasonComputedValuePage)
        )
      )
    }

    "return valid for method six with all answers" in {
      forAll(arbitraryAdaptMethod.arbitrary) {
        adaptMethod =>
          val userAnswers = (for {
            ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
            ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, randomString)
            ua <- ua.set(AdaptMethodPage, adaptMethod)
            ua <- ua.set(ExplainHowYouWillUseMethodSixPage, randomString)

          } yield ua).success.get

          val result = RequestedMethod(userAnswers)

          result shouldBe Valid(
            MethodSix(
              randomString,
              AdaptedMethod(adaptMethod),
              randomString
            )
          )
      }
    }

    "return invalid for method six without ExplainWhyYouHaveNotSelectedMethodOneToFivePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(AdaptMethodPage, AdaptMethod.Method5)
        ua <- ua.set(ExplainHowYouWillUseMethodSixPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainWhyYouHaveNotSelectedMethodOneToFivePage)
      )
    }

    "return invalid for method six without AdaptMethodPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, randomString)
        ua <- ua.set(ExplainHowYouWillUseMethodSixPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(AdaptMethodPage)
      )
    }

    "return invalid for method six without ExplainHowYouWillUseMethodSixPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, randomString)
        ua <- ua.set(AdaptMethodPage, AdaptMethod.Method5)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainHowYouWillUseMethodSixPage)
      )
    }

    "return invalid for method six without answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
          List(AdaptMethodPage, ExplainHowYouWillUseMethodSixPage)
        )
      )
    }

    "return invalid for empty UserAnswers" in {
      val result = RequestedMethod(emptyUserAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ValuationMethodPage)
      )
    }
  }
}

object RequestedMethodSpec extends Generators {
  val randomString: String          = stringsWithMaxLength(8).sample.get
  val draftId: DraftId              = DraftId(1)
  val emptyUserAnswers: UserAnswers = UserAnswers("id", draftId)
}
