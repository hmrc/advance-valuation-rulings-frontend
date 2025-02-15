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
import models.AuthUserType.{IndividualTrader, OrganisationAssistant, OrganisationUser}
import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{DraftId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.AccountHomePage
import play.api.test.FakeRequest
import services.UserAnswersService
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core.{Assistant, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with BeforeAndAfterEach with TableDrivenPropertyChecks {

  private val mockUserAnswersService: UserAnswersService = mock(classOf[UserAnswersService])

  override def beforeEach(): Unit = {
    reset(mockUserAnswersService)
    super.beforeEach()
  }

  class Harness(draftId: DraftId) extends DataRetrievalAction(draftId, mockUserAnswersService) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(
      request
    )
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        when(mockUserAnswersService.get(any())(any())).thenReturn(Future(None))
        val action = new Harness(draftId)

        val result =
          action
            .callTransform(IdentifierRequest(FakeRequest(), "id", "eoriNumber", Individual, None))
            .futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      val scenarios = Table(
        ("affinityGroup", "credentialRole", "expectedAuthUserType"),
        (Individual, None, IndividualTrader),
        (Organisation, Option(User), OrganisationUser),
        (Organisation, Option(Assistant), OrganisationAssistant)
      )

      forAll(scenarios) { (affinityGroup, credentialRole, expectedAuthUserType) =>
        s"must add the AuthUserType $expectedAuthUserType to UserAnswers for $affinityGroup and $credentialRole" in {
          when(mockUserAnswersService.get(any())(any())).thenReturn(
            Future(
              Option(UserAnswers(userAnswersId, draftId))
            )
          )

          val action = new Harness(draftId)

          val result =
            action
              .callTransform(
                IdentifierRequest(
                  FakeRequest(),
                  userAnswersId,
                  EoriNumber,
                  affinityGroup,
                  credentialRole
                )
              )
              .futureValue

          val resultUserType = result.userAnswers.value.get(AccountHomePage)

          resultUserType mustBe defined
          resultUserType.value mustBe expectedAuthUserType
        }
      }
    }

    "when invalid request" - {

      s"must throw exception" in {
        val action = new Harness(draftId)

        val result = intercept[RuntimeException] {
          action
            .callTransform(
              IdentifierRequest(
                FakeRequest(),
                userAnswersId,
                EoriNumber,
                Organisation,
                None
              )
            )
            .futureValue
        }

        result.getMessage mustBe "Auth user type could not be created from request"
      }
    }

  }
}
