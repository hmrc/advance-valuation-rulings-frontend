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

package connectors

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import config.FrontendAppConfig
import models.{Done, DraftId, UserAnswers}
import models.requests.DraftSummaryResponse

class UserAnswersConnector @Inject() (config: FrontendAppConfig, httpClient: HttpClientV2)(implicit
  ec: ExecutionContext
) {

  private val backendUrl = config.advanceValuationRulingsBackendURL

  def set(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .post(url"$backendUrl/user-answers")
      .withBody(Json.toJson(answers))
      .execute
      .map(_ => Done)

  def get(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    httpClient
      .get(url"$backendUrl/user-answers/${draftId.toString}")
      .execute[Option[UserAnswers]]

  def clear(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .delete(url"$backendUrl/user-answers/${draftId.toString}")
      .execute
      .map(_ => Done)

  def keepAlive(draftId: DraftId)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .post(url"$backendUrl/user-answers/${draftId.toString}/keep-alive")
      .execute
      .map(_ => Done)

  def summaries()(implicit headerCarrier: HeaderCarrier): Future[DraftSummaryResponse] =
    httpClient
      .get(url"$backendUrl/user-answers")
      .execute[DraftSummaryResponse]
}
