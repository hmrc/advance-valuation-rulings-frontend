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

package services

import base.SpecBase
import models.AdaptMethod.Method1
import models.ValuationMethod
import models.AuthUserType.IndividualTrader
import models.UserAnswers
import pages._

class DeleteAllUserAnswersServiceSpec extends SpecBase {

  val service = new DeleteAllUserAnswersService

  "DeleteAllUserAnswersServiceSpec" - {

    ".deleteAllUserAnswers" - {

      "should delete all useranswers" in {

        val useranswers: UserAnswers =
          emptyUserAnswers
            .setFuture(AboutSimilarGoodsPage, "foo")
            .futureValue
            .setFuture(AccountHomePage, IndividualTrader)
            .futureValue
            .setFuture(AdaptMethodPage, Method1)
            .futureValue
            .setFuture(AgentForOrgCheckRegisteredDetailsPage, true)
            .futureValue
            .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
            .futureValue

        service.deleteAllUserAnswers(useranswers) mustBe Some(emptyUserAnswers)
      }
    }

    ".deleteAllUserAnswersExcept" - {

      "when given some pages to exclude" - {

        "should return the excluded useranswers" in {

          val useranswers: UserAnswers =
            emptyUserAnswers
              .setFuture(AboutSimilarGoodsPage, "foo")
              .futureValue
              .setFuture(AccountHomePage, IndividualTrader)
              .futureValue
              .setFuture(AdaptMethodPage, Method1)
              .futureValue
              .setFuture(AgentForOrgCheckRegisteredDetailsPage, true)
              .futureValue
              .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
              .futureValue

          val actual =
            service.deleteAllUserAnswersExcept(
              useranswers,
              Seq(AboutSimilarGoodsPage, AdaptMethodPage, AgentForTraderCheckRegisteredDetailsPage)
            )

          val expected =
            Some(
              emptyUserAnswers
                .setFuture(AboutSimilarGoodsPage, "foo")
                .futureValue
                .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
                .futureValue
            )

          actual mustBe expected
        }
      }

      "when given No pages to exclude" - {

        "should return empty useranswers" in {

          val useranswers: UserAnswers =
            emptyUserAnswers
              .setFuture(AboutSimilarGoodsPage, "foo")
              .futureValue
              .setFuture(AccountHomePage, IndividualTrader)
              .futureValue
              .setFuture(AdaptMethodPage, Method1)
              .futureValue
              .setFuture(AgentForOrgCheckRegisteredDetailsPage, true)
              .futureValue
              .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
              .futureValue

          val actual =
            service.deleteAllUserAnswersExcept(
              useranswers,
              Seq()
            )

          val expected =
            Some(emptyUserAnswers)

          actual mustBe expected
        }
      }

      "when given AdaptMethodPage & ValuationMethodPage to exclude be excluded" - {

        "should return the excluded useranswers" in {

          val useranswers: UserAnswers =
            emptyUserAnswers
              .setFuture(AboutSimilarGoodsPage, "foo")
              .futureValue
              .setFuture(AccountHomePage, IndividualTrader)
              .futureValue
              .setFuture(AdaptMethodPage, Method1)
              .futureValue
              .setFuture(ValuationMethodPage, ValuationMethod.Method1)
              .futureValue
              .setFuture(AgentForOrgCheckRegisteredDetailsPage, true)
              .futureValue
              .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
              .futureValue

          val actual =
            service.deleteAllUserAnswersExcept(
              useranswers,
              Seq(AboutSimilarGoodsPage, AdaptMethodPage, ValuationMethodPage, AgentForTraderCheckRegisteredDetailsPage)
            )

          val expected =
            Some(
              emptyUserAnswers
                .setFuture(AboutSimilarGoodsPage, "foo")
                .futureValue
                .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
                .futureValue
                .setFuture(AdaptMethodPage, Method1)
                .futureValue
                .setFuture(ValuationMethodPage, ValuationMethod.Method1)
                .futureValue
            )

          actual mustBe expected
        }
      }

      "when given AdaptMethodPage excluded, while ValuationMethodPage is NOT excluded" - {

        "should delete all answers including AdaptMethodPage useranswers" in {

          val useranswers: UserAnswers =
            emptyUserAnswers
              .setFuture(AboutSimilarGoodsPage, "foo")
              .futureValue
              .setFuture(AccountHomePage, IndividualTrader)
              .futureValue
              .setFuture(AdaptMethodPage, Method1)
              .futureValue
              .setFuture(ValuationMethodPage, ValuationMethod.Method1)
              .futureValue
              .setFuture(AgentForOrgCheckRegisteredDetailsPage, true)
              .futureValue
              .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
              .futureValue

          val actual =
            service.deleteAllUserAnswersExcept(
              useranswers,
              Seq(AboutSimilarGoodsPage, AdaptMethodPage, AgentForTraderCheckRegisteredDetailsPage)
            )

          val expected =
            Some(
              emptyUserAnswers
                .setFuture(AboutSimilarGoodsPage, "foo")
                .futureValue
                .setFuture(AgentForTraderCheckRegisteredDetailsPage, false)
                .futureValue
            )

          actual mustBe expected
        }
      }
    }

  }
}
