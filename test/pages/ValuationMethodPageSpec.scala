/*
 * Copyright 2025 HM Revenue & Customs
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

package pages

import models.{AdaptMethod, DraftId, UserAnswers, ValuationMethod}
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours

import java.time.Instant

class ValuationMethodPageSpec extends PageBehaviours {
  private val fixedInstant = Instant.now()

  "ValuationMethodPage" - {
    beRetrievable[ValuationMethod](ValuationMethodPage)

    beSettable[ValuationMethod](ValuationMethodPage)

    beRemovable[ValuationMethod](ValuationMethodPage)

    "when valuation method 1 is selected" - {
      val userAnswers =
        UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
          .unsafeSet(ValuationMethodPage)(ValuationMethod.Method1)
          .unsafeSet(IsThereASaleInvolvedPage)(true)
          .unsafeSet(IsSaleBetweenRelatedPartiesPage)(true)
          .unsafeSet(ExplainHowPartiesAreRelatedPage)("ExplainHowPartiesAreRelatedPage")
          .unsafeSet(AreThereRestrictionsOnTheGoodsPage)(true)
          .unsafeSet(DescribeTheRestrictionsPage)("DescribeTheRestrictionsPage")
          .unsafeSet(IsTheSaleSubjectToConditionsPage)(true)
          .unsafeSet(DescribeTheConditionsPage)("DescribeTheConditionsPage")

      "on set" - {
        "when the new method is method 1, does not delete any data" in {
          val result = userAnswers.set(ValuationMethodPage, ValuationMethod.Method1)

          result.isSuccess mustEqual true
          result.get.data mustEqual userAnswers.data
        }

        "when the method is different, delete data for method 1" in {
          forAll(Gen.oneOf(ValuationMethod.values.filterNot(_ == ValuationMethod.Method1))) { method =>
            val expectedResult =
              UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
                .unsafeSet(ValuationMethodPage)(method)

            val result = userAnswers.set(ValuationMethodPage, method)

            result.isSuccess mustEqual true
            result.get.data mustEqual expectedResult.data
          }
        }
      }

      "on remove" - {
        "when there is no method value, delete data for method 1" in {
          val expectedResult =
            UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)

          val result = userAnswers.remove(ValuationMethodPage)

          result.isSuccess mustEqual true
          result.get.data mustEqual expectedResult.data
        }
      }
    }

    "when valuation method 2 is selected" - {
      val userAnswers =
        UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
          .unsafeSet(ValuationMethodPage)(ValuationMethod.Method2)
          .unsafeSet(WhyIdenticalGoodsPage)("WhyIdenticalGoodsPageValue")
          .unsafeSet(HaveYouUsedMethodOneInPastPage)(true)
          .unsafeSet(DescribeTheIdenticalGoodsPage)("DescribeTheIdenticalGoodsPageValue")

      "on set" - {
        "when the new method is method 2, does not delete any data" in {
          val result = userAnswers.set(ValuationMethodPage, ValuationMethod.Method2)

          result.isSuccess mustEqual true
          result.get.data mustEqual userAnswers.data
        }

        "when the method is different, delete data for method 2" in {
          forAll(Gen.oneOf(ValuationMethod.values.filterNot(_ == ValuationMethod.Method2))) { method =>
            val expectedResult =
              UserAnswers("userId", DraftId(2), lastUpdated = fixedInstant)
                .unsafeSet(ValuationMethodPage)(method)

            val result = userAnswers.set(ValuationMethodPage, method)

            result.isSuccess mustEqual true
            result.get.data mustEqual expectedResult.data
          }
        }
      }

      "on remove" - {
        "when there is no method value, delete data for method 2" in {
          val expectedResult =
            UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)

          val result = userAnswers.remove(ValuationMethodPage)

          result.isSuccess mustEqual true
          result.get.data mustEqual expectedResult.data
        }
      }
    }

    "when valuation method 3 is selected" - {
      val userAnswers =
        UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
          .unsafeSet(ValuationMethodPage)(ValuationMethod.Method3)
          .unsafeSet(WhyTransactionValueOfSimilarGoodsPage)(
            "WhyTransactionValueOfSimilarGoodsPageValue"
          )
          .unsafeSet(HaveYouUsedMethodOneForSimilarGoodsInPastPage)(true)
          .unsafeSet(DescribeTheSimilarGoodsPage)("DescribeTheSimilarGoodsPageValue")

      "on set" - {
        "when the new method is method 3, does not delete any data" in {
          val result = userAnswers.set(ValuationMethodPage, ValuationMethod.Method3)

          result.isSuccess mustEqual true
          result.get.data mustEqual userAnswers.data
        }

        "when the method is different, delete data for method 3" in {
          forAll(Gen.oneOf(ValuationMethod.values.filterNot(_ == ValuationMethod.Method3))) { method =>
            val expectedResult =
              UserAnswers("userId", DraftId(2), lastUpdated = fixedInstant)
                .unsafeSet(ValuationMethodPage)(method)

            val result = userAnswers.set(ValuationMethodPage, method)

            result.isSuccess mustEqual true
            result.get.data mustEqual expectedResult.data
          }
        }
      }

      "on remove" - {
        "when there is no method value, delete data for method 3" in {
          val expectedResult =
            UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)

          val result = userAnswers.remove(ValuationMethodPage)

          result.isSuccess mustEqual true
          result.get.data mustEqual expectedResult.data
        }
      }
    }

    "when valuation method 4 is selected" - {
      val userAnswers =
        UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
          .unsafeSet(ValuationMethodPage)(ValuationMethod.Method4)
          .unsafeSet(ExplainWhyYouHaveNotSelectedMethodOneToThreePage)(
            "ExplainWhyYouHaveNotSelectedMethodOneToThreePageValue"
          )
          .unsafeSet(ExplainWhyYouChoseMethodFourPage)("ExplainWhyYouChoseMethodFourPage")

      "on set" - {
        "when the new method is method 4, does not delete any data" in {
          val result = userAnswers.set(ValuationMethodPage, ValuationMethod.Method4)

          result.isSuccess mustEqual true
          result.get.data mustEqual userAnswers.data
        }

        "when the method is different, delete data for method 4" in {
          forAll(Gen.oneOf(ValuationMethod.values.filterNot(_ == ValuationMethod.Method4))) { method =>
            val expectedResult =
              UserAnswers("userId", DraftId(2), lastUpdated = fixedInstant)
                .unsafeSet(ValuationMethodPage)(method)

            val result = userAnswers.set(ValuationMethodPage, method)

            result.isSuccess mustEqual true
            result.get.data mustEqual expectedResult.data
          }
        }
      }

      "on remove" - {
        "when there is no method value, delete data for method 4" in {
          val expectedResult =
            UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)

          val result = userAnswers.remove(ValuationMethodPage)

          result.isSuccess mustEqual true
          result.get.data mustEqual expectedResult.data
        }
      }
    }

    "when valuation method 5 is selected" - {
      val userAnswers =
        UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
          .unsafeSet(ValuationMethodPage)(ValuationMethod.Method5)
          .unsafeSet(WhyComputedValuePage)(
            "WhyComputedValuePageValue"
          )
          .unsafeSet(ExplainReasonComputedValuePage)("ExplainReasonComputedValuePageValue")

      "on set" - {
        "when the new method is method 5, does not delete any data" in {
          val result = userAnswers.set(ValuationMethodPage, ValuationMethod.Method5)

          result.isSuccess mustEqual true
          result.get.data mustEqual userAnswers.data
        }

        "when the method is different, delete data for method 5" in {
          forAll(Gen.oneOf(ValuationMethod.values.filterNot(_ == ValuationMethod.Method5))) { method =>
            val expectedResult =
              UserAnswers("userId", DraftId(2), lastUpdated = fixedInstant)
                .unsafeSet(ValuationMethodPage)(method)

            val result = userAnswers.set(ValuationMethodPage, method)

            result.isSuccess mustEqual true
            result.get.data mustEqual expectedResult.data
          }
        }
      }

      "on remove" - {
        "when there is no method value, delete data for method 5" in {
          val expectedResult =
            UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)

          val result = userAnswers.remove(ValuationMethodPage)

          result.isSuccess mustEqual true
          result.get.data mustEqual expectedResult.data
        }
      }
    }

    "when valuation method 6 is selected" - {
      val userAnswers =
        UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)
          .unsafeSet(ValuationMethodPage)(ValuationMethod.Method6)
          .unsafeSet(ExplainWhyYouHaveNotSelectedMethodOneToFivePage)(
            ExplainWhyYouHaveNotSelectedMethodOneToFivePage
          )
          .unsafeSet(AdaptMethodPage)(AdaptMethod.Method1)
          .unsafeSet(ExplainHowYouWillUseMethodSixPage)("ExplainHowYouWillUseMethodSixPageValue")

      "on set" - {
        "when the new method is method 6, does not delete any data" in {
          val result = userAnswers.set(ValuationMethodPage, ValuationMethod.Method6)

          result.isSuccess mustEqual true
          result.get.data mustEqual userAnswers.data
        }

        "when the method is different, delete data for method 6" in {
          forAll(Gen.oneOf(ValuationMethod.values.filterNot(_ == ValuationMethod.Method6))) { method =>
            val expectedResult =
              UserAnswers("userId", DraftId(2), lastUpdated = fixedInstant)
                .unsafeSet(ValuationMethodPage)(method)

            val result = userAnswers.set(ValuationMethodPage, method)

            result.isSuccess mustEqual true
            result.get.data mustEqual expectedResult.data
          }
        }
      }

      "on remove" - {
        "when there is no method value, delete data for method 6" in {
          val expectedResult =
            UserAnswers("userId", DraftId(1), lastUpdated = fixedInstant)

          val result = userAnswers.remove(ValuationMethodPage)

          result.isSuccess mustEqual true
          result.get.data mustEqual expectedResult.data
        }
      }
    }

  }
}
