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

package connectors

import config.Service
import models.upscan.{UpscanInitiateRequest, UpscanInitiateResponse}
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UpscanConnector @Inject() (
  httpClient: HttpClientV2,
  configuration: Configuration
)(implicit ec: ExecutionContext) {

  private val upscanInitiateService: Service =
    configuration.get[Service]("microservice.services.upscan-initiate")

  def initiate(
    request: UpscanInitiateRequest
  )(implicit hc: HeaderCarrier): Future[UpscanInitiateResponse] =
    httpClient
      .post(url"$upscanInitiateService/upscan/v2/initiate")
      .withBody(Json.toJson(request))
      .execute[UpscanInitiateResponse]
}
