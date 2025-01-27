/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.RequestMethod._
import generators.{ApplicationGenerator, TraderDetailsGenerator, UserAnswersGenerator}
import models.requests._
import models.{AcknowledgementReference, DraftId, EoriNumber, TraderDetailsWithCountryCode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import play.api.http.Status
import play.api.libs.json.Json
import utils.{BaseIntegrationSpec, WireMockHelper}

import java.time.Instant

class BackendConnectorSpec
    extends BaseIntegrationSpec
    with WireMockHelper
    with UserAnswersGenerator
    with TraderDetailsGenerator
    with ApplicationGenerator
    with OptionValues {

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

  private val connector = new BackendConnector(appConfig, httpClient)

  ".getTraderDetails" - {

    "OK with bad Json object should return a BackendError of 500" in {
      forAll {
        (
          traderDetailsWithCountryCode: TraderDetailsWithCountryCode,
          acknowledgementReference: AcknowledgementReference
        ) =>
          val eoriNumber       = EoriNumber(traderDetailsWithCountryCode.EORINo)
          val expectedResponse = Json.stringify(Json.toJson("Banana"))

          stub(
            GET,
            traderDetailsRequestUrl(acknowledgementReference, eoriNumber),
            Status.OK,
            expectedResponse
          )
          val result = connector.getTraderDetails(acknowledgementReference, eoriNumber).futureValue

          result.isLeft mustBe true
          result.left.value.code mustBe 500
      }
    }

    "should get trader details from backend" in {
      forAll {
        (
          traderDetailsWithCountryCode: TraderDetailsWithCountryCode,
          acknowledgementReference: AcknowledgementReference
        ) =>
          val eoriNumber       = EoriNumber(traderDetailsWithCountryCode.EORINo)
          val successResponse  = traderDetailsWithCountryCode
          val expectedResponse = Json.stringify(Json.toJson(successResponse))

          stub(
            GET,
            traderDetailsRequestUrl(acknowledgementReference, eoriNumber),
            Status.OK,
            expectedResponse
          )

          val traderDetails =
            connector.getTraderDetails(acknowledgementReference, eoriNumber).futureValue.value

          traderDetails mustBe successResponse
      }
    }

    "should preserve error code for trader details backend error 5xx" in {
      forAll(
        arbitraryEoriNumberGen.arbitrary,
        arbitraryAcknowledgementReferenceGen.arbitrary,
        arbitrary5xxBackendError.arbitrary
      ) { (eoriNumber, acknowledgementReference, backendError) =>
        val expectedResponse = Json.stringify(Json.toJson(backendError))

        stub(
          GET,
          traderDetailsRequestUrl(acknowledgementReference, eoriNumber),
          backendError.code,
          expectedResponse
        )

        val result =
          connector.getTraderDetails(acknowledgementReference, eoriNumber).futureValue.left.value

        result.code mustBe backendError.code
        result.message must include(expectedResponse)
      }
    }

    "should preserve error code for trader details backend error 4xx" in {
      forAll(
        arbitraryEoriNumberGen.arbitrary,
        arbitraryAcknowledgementReferenceGen.arbitrary,
        arbitrary4xxBackendError.arbitrary
      ) { (eoriNumber, acknowledgementReference, backendError) =>
        val expectedResponse = Json.stringify(Json.toJson(backendError))

        stub(
          GET,
          traderDetailsRequestUrl(acknowledgementReference, eoriNumber),
          backendError.code,
          expectedResponse
        )

        val result =
          connector.getTraderDetails(acknowledgementReference, eoriNumber).futureValue.left.value

        result.code mustBe backendError.code
        result.message must include(expectedResponse)
      }
    }
  }

  ".submitApplication" - {

    val applicationId      = ApplicationId(1)
    val response           = ApplicationSubmissionResponse(applicationId)
    val applicationRequest = ApplicationRequest(
      draftId = DraftId(0),
      trader = TraderDetail(
        "traderEori",
        "traderBusinessName",
        "traderLine1",
        Some("traderLine2"),
        Some("traderLine3"),
        "TraderPostcode",
        "GB",
        None,
        Some(false)
      ),
      agent = Some(
        TraderDetail(
          "agentEori",
          "agentBusinessName",
          "agentLine1",
          None,
          None,
          "agentPostcode",
          "agentGB",
          None,
          None
        )
      ),
      contact = ContactDetails("name", "email", None, None, None),
      requestedMethod = MethodOne(None, None, None),
      goodsDetails = GoodsDetails("description", None, None, None, None, None),
      attachments = Nil,
      whatIsYourRole = WhatIsYourRole.AgentOrg,
      letterOfAuthority = None
    )

    "must submit applications to the backend" in {

      wireMockServer.stubFor(
        post(urlEqualTo("/advance-valuation-rulings/applications"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result = connector.submitApplication(applicationRequest).futureValue

      result mustEqual response
    }

    "must return a failed future when an error is returned" in {

      wireMockServer.stubFor(
        post(urlEqualTo("/advance-valuation-rulings/applications"))
          .willReturn(serverError())
      )

      connector.submitApplication(applicationRequest).failed.futureValue
    }
  }

  ".applicationSummaries" - {

    "must return a list of summaries" in {

      val response = ApplicationSummaryResponse(
        Seq(ApplicationSummary(ApplicationId(1), "name", Instant.now, "eori"))
      )

      wireMockServer.stubFor(
        get(urlEqualTo("/advance-valuation-rulings/applications"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result = connector.applicationSummaries.futureValue

      result mustEqual response
    }

    "must return a failed future when an error is returned" in {

      wireMockServer.stubFor(
        get(urlEqualTo("/advance-valuation-rulings/applications"))
          .willReturn(serverError())
      )

      connector.applicationSummaries.failed.futureValue
    }
  }

  ".getApplication" - {

    "must get an application from the backend" in {

      forAll(arbitrary[Application], minSuccessful(1)) { application =>
        wireMockServer.stubFor(
          get(urlEqualTo(s"/advance-valuation-rulings/applications/${application.id.toString}"))
            .willReturn(ok(Json.toJson(application).toString))
        )

        val result = connector.getApplication(application.id.toString).futureValue

        result mustEqual application
      }
    }

    "must return a failed future when an error is returned" in {

      forAll(arbitrary[Application], minSuccessful(1)) { application =>
        wireMockServer.stubFor(
          get(urlEqualTo(s"/advance-valuation-rulings/applications${application.id.toString}"))
            .willReturn(serverError())
        )

        connector.getApplication(application.id.toString).failed.futureValue
      }
    }
  }
}
