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

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import models.{DraftId, UserAnswers}
import repositories.SessionRepository

class UserAnswersService @Inject() (sessionRepository: SessionRepository)(implicit
  ec: ExecutionContext
) {

  def keepAlive(userId: String, draftId: DraftId): Future[Boolean] =
    sessionRepository.keepAlive(userId, draftId)

  def get(userId: String, draftId: DraftId): Future[Option[UserAnswers]] =
    sessionRepository.get(userId, draftId)

  def set(answers: UserAnswers): Future[Boolean] =
    sessionRepository.set(answers)

  def clear(userId: String, draftId: DraftId): Future[Boolean] =
    sessionRepository.clear(userId, draftId)
}
