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

package services

import scala.concurrent.Future

import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import connectors.UserAnswersConnector
import models.{Done, DraftId, UserAnswers}
import models.requests.DraftSummaryResponse
import org.mockito.{Mockito, MockitoSugar}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UserAnswersServiceSpec
    extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaFutures
    with OptionValues {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockUserAnswersConnector = mock[UserAnswersConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockUserAnswersConnector)
    super.beforeEach()
  }

  private val userId  = "user id"
  private val draftId = DraftId(0)
  private val answers = UserAnswers(userId, draftId, Json.obj())
  private val service = new UserAnswersService(mockUserAnswersConnector)

  ".get" - {

    "must return user answers when they exist in the repository" in {

      when(mockUserAnswersConnector.get(eqTo(draftId))(any()))
        .thenReturn(Future.successful(Some(answers)))

      val result = service.get(draftId).futureValue
      result.value mustEqual answers
    }

    "must return None when answers do not exist in the repository" in {

      when(mockUserAnswersConnector.get(eqTo(draftId))(any())).thenReturn(Future.successful(None))

      val result = service.get(draftId).futureValue
      result must not be defined
    }
  }

  ".set" - {

    "must save user answers to the repository" in {

      when(mockUserAnswersConnector.set(any())(any())).thenReturn(Future.successful(Done))

      service.set(answers).futureValue
      verify(mockUserAnswersConnector, times(1)).set(eqTo(answers))(any())
    }
  }

  ".keepAlive" - {

    "must keep user answers alive" in {

      when(mockUserAnswersConnector.keepAlive(any())(any())).thenReturn(Future.successful(Done))

      service.keepAlive(draftId).futureValue
      verify(mockUserAnswersConnector, times(1)).keepAlive(eqTo(draftId))(any())
    }
  }

  ".clear" - {

    "must keep user answers alive" in {

      when(mockUserAnswersConnector.clear(any())(any())).thenReturn(Future.successful(Done))

      service.clear(draftId).futureValue
      verify(mockUserAnswersConnector, times(1)).clear(eqTo(draftId))(any())
    }
  }

  ".summaries" - {

    "must return the summary response provided by the connector" in {

      when(mockUserAnswersConnector.summaries()(any()))
        .thenReturn(Future.successful(DraftSummaryResponse(Nil)))

      val result = service.summaries().futureValue
      result mustEqual DraftSummaryResponse(Nil)
    }
  }
}
