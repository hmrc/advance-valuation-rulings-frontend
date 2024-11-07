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

import config.FrontendAppConfig
import models.requests.EmailRequest
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailConnector @Inject() (
  httpClient: HttpClientV2,
  appConfig: FrontendAppConfig
)(using ec: ExecutionContext) {

  def sendEmail(
    emailRequest: EmailRequest
  )(using hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .post(url"${appConfig.emailBaseUrl}/hmrc/email")
      .withBody(Json.toJson(emailRequest))
      .execute[HttpResponse](using throwOnFailure(readEitherOf(using readRaw)))
}
