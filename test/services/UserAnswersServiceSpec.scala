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

import models.{DraftId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserAnswersServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach with ScalaFutures with OptionValues {

  private val mockRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRepository)
    super.beforeEach()
  }

  private val userId = "user id"
  private val draftId = DraftId(0)
  private val answers = UserAnswers(userId, draftId, Json.obj())
  private val service = new UserAnswersService(mockRepository)

  ".get" - {

    "must return user answers when they exist in the repository" in {

      when(mockRepository.get(eqTo(userId), eqTo(draftId))).thenReturn(Future.successful(Some(answers)))

      val result = service.get(userId, draftId).futureValue
      result.value mustEqual answers
    }

    "must return None when answers do not exist in the repository" in {

      when(mockRepository.get(eqTo(userId), eqTo(draftId))).thenReturn(Future.successful(None))

      val result = service.get(userId, draftId).futureValue
      result must not be defined
    }
  }

  ".set" - {

    "must save user answers to the repository" in {

      when(mockRepository.set(any())).thenReturn(Future.successful(true))

      service.set(answers).futureValue
      verify(mockRepository, times(1)).set(eqTo(answers))
    }
  }

  ".keepAlive" - {

    "must keep user answers alive" in {

      when(mockRepository.keepAlive(any(), any())).thenReturn(Future.successful(true))

      service.keepAlive(userId, draftId).futureValue
      verify(mockRepository, times(1)).keepAlive(eqTo(userId), eqTo(draftId))
    }
  }

  ".clear" - {

    "must keep user answers alive" in {

      when(mockRepository.clear(any(), any())).thenReturn(Future.successful(true))

      service.clear(userId, draftId).futureValue
      verify(mockRepository, times(1)).clear(eqTo(userId), eqTo(draftId))
    }
  }
}
