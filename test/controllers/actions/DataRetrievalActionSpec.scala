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

package controllers.actions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{Admin, Assistant, InsufficientEnrolments, User}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}

import base.SpecBase
import models.{DraftId, UserAnswers}
import models.AuthUserType.{IndividualTrader, OrganisationAdmin, OrganisationAssistant}
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages.AccountHomePage
import services.UserAnswersService

class DataRetrievalActionSpec
    extends SpecBase
    with MockitoSugar
    with BeforeAndAfterEach
    with TableDrivenPropertyChecks {

  private val mockUserAnswersService = mock[UserAnswersService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockUserAnswersService)
    super.beforeEach()
  }

  class Harness(draftId: DraftId) extends DataRetrievalAction(draftId, mockUserAnswersService) {
    def callTransform[A](request: IdentifierRequest[A]): Future[DataRequest[A]] = transform(
      request
    )
  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must throw exception when unable to retrieve user answers" in {

        when(mockUserAnswersService.get(any())(any())) thenReturn Future(None)
        val action = new Harness(draftId)

        the[IllegalStateException] thrownBy {
          action
            .callTransform(
              IdentifierRequest(FakeRequest(), userAnswersId, EoriNumber, Individual, None)
            )
            .futureValue
        } should have message "Unable to retrieve user answers"
      }
    }

    "must throw exception when the auth user type could not be determined" in {

      val scenarios = Table("credentialRole", None, Option(User), Option(Admin), Option(Assistant))

      forAll(scenarios) {
        credentialRole =>
          when(mockUserAnswersService.get(any())(any())) thenReturn Future(None)
          val action = new Harness(draftId)

          the[InsufficientEnrolments] thrownBy {
            action
              .callTransform(
                IdentifierRequest(FakeRequest(), userAnswersId, EoriNumber, Agent, credentialRole)
              )
              .futureValue
          } should have message "Auth user type could not be created from request"
      }
    }

    "when there is data in the cache" - {

      val scenarios = Table(
        ("affinityGroup", "credentialRole", "expectedAuthUserType"),
        (Individual, None, IndividualTrader),
        (Organisation, Option(User), OrganisationAdmin),
        (Organisation, Option(Admin), OrganisationAdmin),
        (Organisation, Option(Assistant), OrganisationAssistant)
      )

      forAll(scenarios) {
        (affinityGroup, credentialRole, expectedAuthUserType) =>
          s"must add the AuthUserType $expectedAuthUserType to UserAnswers for $affinityGroup and $credentialRole" in {
            when(mockUserAnswersService.get(any())(any())) thenReturn Future(
              Option(UserAnswers(userAnswersId, draftId))
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

            val resultUserType = result.userAnswers.get(AccountHomePage)

            resultUserType mustBe defined
            resultUserType.value mustBe expectedAuthUserType
          }
      }
    }
  }
}
