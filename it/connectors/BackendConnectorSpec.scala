package connectors

import play.api.http.Status
import play.api.libs.json.Json

import com.github.tomakehurst.wiremock.http.RequestMethod._
import generators.{TraderDetailsGenerator, UserAnswersGenerator, ValuationRulingApplicationGenerator}
import models.{AcknowledgementReference, EoriNumber, TraderDetailsWithCountryCode, UserAnswers, ValuationRulingsApplication}
import models.requests.ApplicationRequest
import utils.{BaseIntegrationSpec, WireMockHelper}

class BackendConnectorSpec
    extends BaseIntegrationSpec
    with WireMockHelper
    with UserAnswersGenerator
    with TraderDetailsGenerator
    with ValuationRulingApplicationGenerator {

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

    "should return BAD_GATEWAY error for trader details backend error 5xx" in {
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

          val traderDetails =
            connector.getTraderDetails(acknowledgementReference, eoriNumber).futureValue.left.value

          traderDetails.code mustBe Status.BAD_GATEWAY
          traderDetails.message must include(expectedResponse)
      }
    }

    "should return INTERNAL_SERVER_ERROR error for trader details backend error 4xx" in {
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

          val traderDetails =
            connector.getTraderDetails(acknowledgementReference, eoriNumber).futureValue.left.value

          traderDetails.code mustBe Status.INTERNAL_SERVER_ERROR
          traderDetails.message must include(expectedResponse)
      }
    }
  }

  ".submit" - {
    "should submit user answers to backend" in {
      forAll {
        userAnswers: UserAnswers =>
          val requestBody = Json.stringify(Json.toJson(userAnswers))

          stub(
            POST,
            submitAnswersEndpoint,
            Status.OK,
            responseBody = "some response",
            requestBody = Option(requestBody)
          )

          val result = connector.submitAnswers(userAnswers).futureValue.value

          result.status mustBe Status.OK
      }
    }
  }

  ".submitApplication" - {
    "should submit application to backend" ignore {
      forAll {
        applicationRequest: ApplicationRequest =>
          // val requestBody = Json.stringify(Json.toJson(applicationRequest))

          // stub(
          //   POST,
          //   applicationEndpoint,
          //   Status.OK,
          //   responseBody = "some response",
          //   requestBody = Option(requestBody)
          // )

          val result = connector.submitApplication(applicationRequest).futureValue.value

          result.status mustBe Status.OK
      }
    }
  }

  ".application" - {
    "should get application from backend" ignore {
      forAll {
        (
          application: ValuationRulingsApplication,
        ) =>
          // val expectedResponse = Json.stringify(Json.toJson(application))

          // stub(
          //   GET,
          //   getApplicationRequestUrl(application.applicationNumber),
          //   Status.OK,
          //   expectedResponse
          // )

          val response =
            connector
              .submitApplication(application.data)
              .flatMap(_ => connector.getApplication(application.applicationNumber))
              .futureValue
              .value

          response mustBe application
      }
    }
  }
}
