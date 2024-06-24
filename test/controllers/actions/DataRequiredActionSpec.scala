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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.DraftId
import models.requests.{DataRequest, OptionalDataRequest}
import org.mockito.MockitoSugar.{mock, reset}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ControllerComponents, Result}
import play.api.test.Helpers.GET
import play.api.test.{FakeRequest, Helpers}
import services.UserAnswersService
import uk.gov.hmrc.auth.core.AffinityGroup.Individual

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec extends SpecBase {

  private val mockUserAnswersService = mock[UserAnswersService]

  override def beforeEach(): Unit = {
    reset(mockUserAnswersService)
    super.beforeEach()
  }

  implicit val ec: ExecutionContext = mock[ExecutionContext]
  val cc: ControllerComponents      = Helpers.stubControllerComponents()

  class Harness(draftId: DraftId) extends DataRequiredActionImpl {
    def callRefine[A](req: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] =
      refine(req)

  }

  "Data Required Action " - {
    "returns a Left when" - {
      "there are no user answers" - {
        "with a /cancel url" in {

          val sut = new Harness(DraftId("0123").get)
          val req =
            OptionalDataRequest(
              FakeRequest(GET, "/cancel"),
              "userId",
              "eoriNumber",
              Individual,
              None,
              None
            )

          val result =
            sut.callRefine(OptionalDataRequest(req, "userId", "eoriNumber", Individual, None, None))

          whenReady(result) { re =>
            re.isLeft mustBe true
            re.left.value mustBe Redirect(
              routes.YourApplicationHasBeenCancelledController.onPageLoad()
            )
          }

        }
        "for url other than /cancel" in {

          val sut = new Harness(DraftId("0123").get)
          val req =
            OptionalDataRequest(FakeRequest(), "userId", "eoriNumber", Individual, None, None)

          val result =
            sut.callRefine(OptionalDataRequest(req, "userId", "eoriNumber", Individual, None, None))

          whenReady(result) { re =>
            re.isLeft mustBe true
            re.left.value mustBe Redirect(
              routes.JourneyRecoveryController.onPageLoad()
            )
          }

        }
      }
    }
  }
}
