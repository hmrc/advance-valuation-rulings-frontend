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

import connectors.UserAnswersConnector
import models.{Done, DraftId, UserAnswers}
import models.requests.DraftSummaryResponse

class UserAnswersService @Inject() (userAnswersConnector: UserAnswersConnector) {

  def keepAlive(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Done] =
    userAnswersConnector.keepAlive(draftId)

  def get(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    userAnswersConnector.get(draftId)

  def getInternal(draftId: DraftId)(hc: HeaderCarrier): Future[Option[UserAnswers]] =
    ???

  def set(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] =
    userAnswersConnector.set(answers)

  def setInternal(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] =
    ???

  def clear(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Done] =
    userAnswersConnector.clear(draftId)

  def summaries()(implicit hc: HeaderCarrier): Future[DraftSummaryResponse] =
    userAnswersConnector.summaries()
}
