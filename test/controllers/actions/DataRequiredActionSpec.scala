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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.AuthUserType.IndividualTrader
import models.{DraftId, UserAnswers}
import models.requests.{DataRequest, OptionalDataRequest}
import pages.AccountHomePage
import play.api.mvc.{AnyContent, Result}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Individual

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec extends SpecBase {

  private given ec: ExecutionContext = stubControllerComponents().executionContext

  private class Harness extends DataRequiredActionImpl {
    def callRefine[A](req: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] =
      refine(req)
  }

  "Data Required Action " - {
    "returns a Left when" - {
      "there are no user answers" - {
        "with a /cancel url" in {
          val req: OptionalDataRequest[AnyContent] =
            OptionalDataRequest(
              FakeRequest(GET, "/cancel"),
              "userId",
              "eoriNumber",
              Individual,
              None,
              None
            )

          val sut: Harness = new Harness

          val result: Either[Result, DataRequest[AnyContent]] = sut.callRefine(req).futureValue

          result.isLeft mustBe true

          val redirectResult: Result = result.left.value

          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header
            .headers(LOCATION) mustBe routes.YourApplicationHasBeenCancelledController.onPageLoad().url
        }

        "for url other than /cancel" in {
          val req: OptionalDataRequest[AnyContent] =
            OptionalDataRequest(FakeRequest(), "userId", "eoriNumber", Individual, None, None)

          val sut: Harness = new Harness

          val result: Either[Result, DataRequest[AnyContent]] = sut.callRefine(req).futureValue

          result.isLeft mustBe true

          val redirectResult: Result = result.left.value

          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "user answers do not contain AccountHomePage" in {
        val userAnswers: UserAnswers = UserAnswers(userId = "userId", DraftId(0))

        val req: OptionalDataRequest[AnyContent] =
          OptionalDataRequest(FakeRequest(), "userId", "eoriNumber", Individual, None, Some(userAnswers))

        val sut: Harness = new Harness

        val result: Either[Result, DataRequest[AnyContent]] = sut.callRefine(req).futureValue

        result.isLeft mustBe true

        val redirectResult: Result = result.left.value

        redirectResult.header.status mustBe SEE_OTHER
        redirectResult.header.headers(LOCATION) mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "returns a Right when" - {
      "user answers contain AccountHomePage" in {
        val userAnswers: UserAnswers =
          UserAnswers(userId = "userId", DraftId(0))
            .set(AccountHomePage, IndividualTrader)
            .success
            .value

        val req: OptionalDataRequest[AnyContent] =
          OptionalDataRequest(FakeRequest(), "userId", "eoriNumber", Individual, None, Some(userAnswers))

        val sut: Harness = new Harness

        val result: Either[Result, DataRequest[AnyContent]] = sut.callRefine(req).futureValue

        result.isRight mustBe true

        result.value mustBe DataRequest(
          req.request,
          req.userId,
          req.eoriNumber,
          userAnswers,
          req.affinityGroup,
          req.credentialRole
        )
      }
    }
  }
}
