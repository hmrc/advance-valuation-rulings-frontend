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

package viewmodels.application

import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

import models.AdaptMethod
import models.ValuationMethod._
import models.requests._
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class RequestedMethodSummarySpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  private implicit val m: Messages = stubMessages()

  ".rows" - {

    "must render the correct rows" - {

      "for method 1 with optional values not present" in {

        val method = MethodOne(None, None, None)

        RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
          SummaryListRow(
            Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
            Value(Text(m(s"valuationMethod.${Method1.toString}")))
          ),
          SummaryListRow(
            Key(Text(m("isThereASaleInvolved.checkYourAnswersLabel"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("isSaleBetweenRelatedParties.checkYourAnswersLabel"))),
            Value(Text(m("site.no")))
          ),
          SummaryListRow(
            Key(Text(m("areThereRestrictionsOnTheGoods.title"))),
            Value(Text(m("site.no")))
          ),
          SummaryListRow(
            Key(Text(m("isTheSaleSubjectToConditions.title"))),
            Value(Text(m("site.no")))
          )
        )
      }

      "for method 1 with all optional values present" in {

        val method = MethodOne(Some("related parties"), Some("conditions"), Some("restrictions"))

        RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
          SummaryListRow(
            Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
            Value(Text(m(s"valuationMethod.${Method1.toString}")))
          ),
          SummaryListRow(
            Key(Text(m("isThereASaleInvolved.checkYourAnswersLabel"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("isSaleBetweenRelatedParties.checkYourAnswersLabel"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("explainHowPartiesAreRelated.checkYourAnswersLabel"))),
            Value(Text(method.saleBetweenRelatedParties.get))
          ),
          SummaryListRow(
            Key(Text(m("areThereRestrictionsOnTheGoods.title"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("describeTheRestrictions.checkYourAnswersLabel"))),
            Value(Text(method.goodsRestrictions.get))
          ),
          SummaryListRow(
            Key(Text(m("isTheSaleSubjectToConditions.title"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("describeTheConditions.checkYourAnswersLabel"))),
            Value(Text(method.saleConditions.get))
          )
        )
      }

      "for method 2" in {

        val method = MethodTwo("why", PreviousIdenticalGoods("identical goods"))

        RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
          SummaryListRow(
            Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
            Value(Text(m(s"valuationMethod.${Method2.toString}")))
          ),
          SummaryListRow(
            Key(Text(m("whyIdenticalGoods.checkYourAnswersLabel"))),
            Value(Text(method.whyNotOtherMethods))
          ),
          SummaryListRow(
            Key(Text(m("haveYouUsedMethodOneInPast.checkYourAnswersLabel"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("describeTheIdenticalGoods.checkYourAnswersLabel"))),
            Value(Text(method.previousIdenticalGoods.value))
          )
        )
      }

      "for method 3" in {

        val method = MethodThree("why", PreviousSimilarGoods("identical goods"))

        RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
          SummaryListRow(
            Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
            Value(Text(m(s"valuationMethod.${Method3.toString}")))
          ),
          SummaryListRow(
            Key(Text(m("whyTransactionValueOfSimilarGoods.checkYourAnswersLabel"))),
            Value(Text(method.whyNotOtherMethods))
          ),
          SummaryListRow(
            Key(Text(m("haveYouUsedMethodOneForSimilarGoodsInPast.checkYourAnswersLabel"))),
            Value(Text(m("site.yes")))
          ),
          SummaryListRow(
            Key(Text(m("describeTheSimilarGoods.checkYourAnswersLabel"))),
            Value(Text(method.previousSimilarGoods.value))
          )
        )
      }

      "for method 4" in {

        val method = MethodFour("why", "deductive method")

        RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
          SummaryListRow(
            Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
            Value(Text(m(s"valuationMethod.${Method4.toString}")))
          ),
          SummaryListRow(
            Key(Text(m("explainWhyYouHaveNotSelectedMethodOneToThree.checkYourAnswersLabel"))),
            Value(Text(method.whyNotOtherMethods))
          ),
          SummaryListRow(
            Key(Text(m("explainWhyYouChoseMethodFour.checkYourAnswersLabel"))),
            Value(Text(method.deductiveMethod))
          )
        )
      }

      "for method 5" in {

        val method = MethodFive("why", "computed value")

        RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
          SummaryListRow(
            Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
            Value(Text(m(s"valuationMethod.${Method5.toString}")))
          ),
          SummaryListRow(
            Key(Text(m("whyComputedValue.checkYourAnswersLabel"))),
            Value(Text(method.whyNotOtherMethods))
          ),
          SummaryListRow(
            Key(Text(m("explainReasonComputedValue.checkYourAnswersLabel"))),
            Value(Text(method.computedValue))
          )
        )
      }

      "for method 6" in {

        forAll(Gen.oneOf(AdaptedMethod.values)) {
          adaptedMethod =>
            val adaptMethod = adaptedMethod match {
              case AdaptedMethod.MethodOne   => AdaptMethod.Method1
              case AdaptedMethod.MethodTwo   => AdaptMethod.Method2
              case AdaptedMethod.MethodThree => AdaptMethod.Method3
              case AdaptedMethod.MethodFour  => AdaptMethod.Method4
              case AdaptedMethod.MethodFive  => AdaptMethod.Method5
              case AdaptedMethod.Unable      => AdaptMethod.NoOtherMethod
            }

            val method = MethodSix("why", adaptedMethod, "valuation")

            RequestedMethodSummary.rows(method) must contain theSameElementsInOrderAs Seq(
              SummaryListRow(
                Key(Text(m("valuationMethod.checkYourAnswersLabel"))),
                Value(Text(m(s"valuationMethod.${Method6.toString}")))
              ),
              SummaryListRow(
                Key(Text(m("explainWhyYouHaveNotSelectedMethodOneToFive.checkYourAnswersLabel"))),
                Value(Text(method.whyNotOtherMethods))
              ),
              SummaryListRow(
                Key(Text(m("adaptMethod.checkYourAnswersLabel"))),
                Value(Text(m(s"adaptMethod.$adaptMethod")))
              ),
              SummaryListRow(
                Key(Text(m("explainHowYouWillUseMethodSix.checkYourAnswersLabel"))),
                Value(Text(method.valuationDescription))
              )
            )
        }
      }
    }
  }
}
