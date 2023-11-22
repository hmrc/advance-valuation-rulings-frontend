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

package config

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.Status.{CREATED, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.AUTHORIZATION
import utils.{BaseIntegrationSpec, WireMockHelper}

class InternalAuthInitialiserSpec extends BaseIntegrationSpec with IntegrationPatience with WireMockHelper {

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

  "when configured to run" - {

    "must initialise the internal-auth token if it is not already initialised" in {

      val authToken = "authToken"
      val appName   = "appName"

      val expectedRequest = Json.obj(
        "token"       -> authToken,
        "principal"   -> appName,
        "permissions" -> Seq(
          Json.obj(
            "resourceType"     -> "object-store",
            "resourceLocation" -> "advance-valuation-rulings-frontend",
            "actions"          -> List("READ", "WRITE", "DELETE")
          ),
          Json.obj(
            "resourceType"     -> "advance-valuation-rulings",
            "resourceLocation" -> "*",
            "actions"          -> List("*")
          )
        )
      )

      wireMockServer.stubFor(
        get(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      wireMockServer.stubFor(
        post(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(CREATED))
      )

      GuiceApplicationBuilder()
        .configure(
          "microservice.services.internal-auth.port" -> wireMockServer.port(),
          "appName"                                  -> appName,
          "create-internal-auth-token-on-start"      -> true,
          "internal-auth.token"                      -> authToken
        )
        .build()

      wireMockServer.verify(
        1,
        getRequestedFor(urlMatching("/test-only/token"))
          .withHeader(AUTHORIZATION, equalTo(authToken))
      )
      wireMockServer.verify(
        1,
        postRequestedFor(urlMatching("/test-only/token"))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(expectedRequest))))
      )
    }

    "must fail with exception the internal-auth token if it is not already initialised" in {

      val authToken = "authToken"
      val appName   = "appName"

      val expectedRequest = Json.obj(
        "token"       -> authToken,
        "principal"   -> appName,
        "permissions" -> Seq(
          Json.obj(
            "resourceType"     -> "object-store",
            "resourceLocation" -> "advance-valuation-rulings-frontend",
            "actions"          -> List("READ", "WRITE", "DELETE")
          ),
          Json.obj(
            "resourceType"     -> "advance-valuation-rulings",
            "resourceLocation" -> "*",
            "actions"          -> List("*")
          )
        )
      )

      wireMockServer.stubFor(
        get(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      wireMockServer.stubFor(
        post(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(OK))
      )

      val exception = intercept[RuntimeException] {
        GuiceApplicationBuilder()
          .configure(
            "microservice.services.internal-auth.port" -> wireMockServer.port(),
            "appName"                                  -> appName,
            "create-internal-auth-token-on-start"      -> true,
            "internal-auth.token"                      -> authToken
          )
          .build()
      }

      exception.getMessage must include("Unable to initialise internal-auth token")

      wireMockServer.verify(
        1,
        getRequestedFor(urlMatching("/test-only/token"))
          .withHeader(AUTHORIZATION, equalTo(authToken))
      )
      wireMockServer.verify(
        1,
        postRequestedFor(urlMatching("/test-only/token"))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(expectedRequest))))
      )
    }

    "must not initialise the internal-auth token if it is already initialised" in {

      val authToken = "authToken"
      val appName   = "appName"

      wireMockServer.stubFor(
        get(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(OK))
      )

      wireMockServer.stubFor(
        post(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(CREATED))
      )

      val app = GuiceApplicationBuilder()
        .configure(
          "microservice.services.internal-auth.port" -> wireMockServer.port(),
          "appName"                                  -> appName,
          "create-internal-auth-token-on-start"      -> true,
          "internal-auth.token"                      -> authToken
        )
        .build()

      app.injector.instanceOf[config.InternalAuthTokenInitialiser].initialised.futureValue

      wireMockServer.verify(
        1,
        getRequestedFor(urlMatching("/test-only/token"))
          .withHeader(AUTHORIZATION, equalTo(authToken))
      )
      wireMockServer.verify(0, postRequestedFor(urlMatching("/test-only/token")))
    }
  }

  "when not configured to run" - {

    "must not make the relevant calls to internal-auth" in {

      val authToken = "authToken"
      val appName   = "appName"

      wireMockServer.stubFor(
        get(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(OK))
      )

      wireMockServer.stubFor(
        post(urlMatching("/test-only/token"))
          .willReturn(aResponse().withStatus(CREATED))
      )

      val app = GuiceApplicationBuilder()
        .configure(
          "microservice.services.internal-auth.port" -> wireMockServer.port(),
          "appName"                                  -> appName,
          "create-internal-auth-token-on-start"      -> false,
          "internal-auth.token"                      -> authToken
        )
        .build()

      app.injector.instanceOf[InternalAuthTokenInitialiser].initialised.futureValue

      wireMockServer.verify(0, getRequestedFor(urlMatching("/test-only/token")))
      wireMockServer.verify(0, postRequestedFor(urlMatching("/test-only/token")))
    }
  }
}
