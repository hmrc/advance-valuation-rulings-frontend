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

import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json

import com.github.tomakehurst.wiremock.client.WireMock.{postRequestedFor, urlEqualTo, verify}
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import models.requests.EmailRequest
import utils.{BaseIntegrationSpec, WireMockHelper}

class EmailConnectorSpec extends BaseIntegrationSpec with WireMockHelper {
  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWireMock()
  }

  private val connector = new EmailConnector(httpClient, appConfig)

  "Email Connector" - {

    "must send an email" in {

      val emailRequest = EmailRequest(
        to = List(),
        templateId = "???",
        parameters = Map.empty,
        eventUrl = None,
        onSendUrl = None,
        auditData = Map.empty
      )
      val requestBody  = Json.stringify(Json.toJson(emailRequest))

      stub(POST, "/email", 202, "", Some(requestBody))

      assert(connector.sendEmail(emailRequest).futureValue.status === ACCEPTED)
      verify(1, postRequestedFor(urlEqualTo(s"/email")))
    }

    "must return a failed future when the server response with an error" in {

      val emailRequest = EmailRequest(
        to = List(),
        templateId = "???",
        parameters = Map.empty,
        eventUrl = None,
        onSendUrl = None,
        auditData = Map.empty
      )
      val requestBody  = Json.stringify(Json.toJson(emailRequest))

      stub(POST, "/email", 500, "", Some(requestBody))

      connector.sendEmail(emailRequest).failed.futureValue
    }
  }
}
