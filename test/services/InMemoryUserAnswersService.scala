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

import scala.concurrent.Future

import uk.gov.hmrc.http.HeaderCarrier

import models.{Done, DraftId, UserAnswers}
import models.requests.{DraftSummary, DraftSummaryResponse}

class InMemoryUserAnswersService @Inject() () extends UserAnswersService {

  private val store = scala.collection.mutable.Map.empty[DraftId, UserAnswers]

  def keepAlive(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Done] =
    Future.successful(Done)

  def get(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    Future.successful(store.get(draftId))

  def getInternal(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    Future.successful(store.get(draftId))

  def set(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] = {
    store += (answers.draftId -> answers)
    Future.successful(Done)
  }

  def setInternal(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] = {
    store += (answers.draftId -> answers)
    Future.successful(Done)
  }

  def clear(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Done] = {
    store -= draftId
    Future.successful(Done)
  }

  def summaries()(implicit hc: HeaderCarrier): Future[DraftSummaryResponse] =
    Future.successful(
      DraftSummaryResponse(
        store.map {
          case (key, value) =>
            DraftSummary(
              id = key,
              goodsName = value.get(pages.DescriptionOfGoodsPage),
              lastUpdated = value.lastUpdated,
              eoriNumber = None
            )
        }.toList
      )
    )
}
