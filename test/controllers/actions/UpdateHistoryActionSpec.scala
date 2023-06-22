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
import play.api.test.Helpers.GET
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import controllers.actions._
import models.{Done, NormalMode}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.HasConfidentialInformationPage
import queries.LastQuestionViewed
import services.UserAnswersService

class UpdateHistoryActionSpec extends SpecBase with MockitoSugar {

  "UpdateHistoryAction" - {

    "must update the user's last page" in {
      val mockService     = mock[UserAnswersService]
      val expectedAnswers =
        emptyUserAnswers.set(LastQuestionViewed, HasConfidentialInformationPage).success.value

      val action      =
        new UserAnswersHistoryActionProvider(mockService).apply(HasConfidentialInformationPage)
      val fakeRequest =
        FakeRequest(
          GET,
          controllers.routes.HasConfidentialInformationController
            .onPageLoad(NormalMode, draftId)
            .url
        )
      val dataRequest =
        DataRequest(
          request = fakeRequest,
          userId = "id",
          eoriNumber = "eori",
          userAnswers = emptyUserAnswers,
          affinityGroup = AffinityGroup.Individual,
          credentialRole = None
        )

      when(mockService.set(any())(any())).thenReturn(Future.successful(Done))

      val result = action.refine(dataRequest).futureValue

      verify(mockService, times(1)).set(eqTo(expectedAnswers))(any())

      result.map(_.userAnswers.get(queries.LastQuestionViewed)) mustBe Right(
        Some(HasConfidentialInformationPage)
      )
    }
  }
}
