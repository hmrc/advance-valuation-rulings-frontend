package connectors

import java.time.Instant

import play.api.http.Status
import play.api.libs.json.Json

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.RequestMethod._
import generators.{ApplicationGenerator, TraderDetailsGenerator, UserAnswersGenerator}
import models.{AcknowledgementReference, DraftId, EoriNumber, TraderDetailsWithCountryCode}
import models.requests._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import utils.{BaseIntegrationSpec, WireMockHelper}

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
          result.left.get.code mustBe 500
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
      ) {
        (eoriNumber, acknowledgementReference, backendError) =>
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
      ) {
        (eoriNumber, acknowledgementReference, backendError) =>
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
        None
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
          None
        )
      ),
      contact = ContactDetails("name", "email", None),
      requestedMethod = MethodOne(None, None, None),
      goodsDetails = GoodsDetails("name", "description", None, None, None),
      attachments = Nil
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

      forAll(arbitrary[Application], minSuccessful(1)) {
        application =>
          wireMockServer.stubFor(
            get(urlEqualTo(s"/advance-valuation-rulings/applications/${application.id.toString}"))
              .willReturn(ok(Json.toJson(application).toString))
          )

          val result = connector.getApplication(application.id.toString).futureValue

          result mustEqual application
      }
    }

    "must return a failed future when an error is returned" in {

      forAll(arbitrary[Application], minSuccessful(1)) {
        application =>
          wireMockServer.stubFor(
            get(urlEqualTo(s"/advance-valuation-rulings/applications${application.id.toString}"))
              .willReturn(serverError())
          )

          connector.getApplication(application.id.toString).failed.futureValue
      }
    }
  }
}
