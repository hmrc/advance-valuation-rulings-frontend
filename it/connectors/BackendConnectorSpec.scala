package connectors

import play.api.http.Status
import play.api.libs.json.Json

import com.github.tomakehurst.wiremock.http.RequestMethod._
import generators.{Generators, TraderDetailsGenerator}
import models.{AcknowledgementReference, EoriNumber, TraderDetailsWithCountryCode, UserAnswers}
import models.fileupload.ReadyCallbackBody
import utils.{BaseIntegrationSpec, WireMockHelper}

class BackendConnectorSpec
    extends BaseIntegrationSpec
    with WireMockHelper
    with TraderDetailsGenerator
    with Generators {

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

  ".putFile" - {
    "should put file on object store" in {
      forAll {
        callback: ReadyCallbackBody =>
          val request     = BackendConnector.ObjectStorePutRequest(callback)
          val requestBody = Json.stringify(Json.toJson(request))

          stub(
            POST,
            fileUploadEndpoint,
            Status.OK,
            responseBody = "some response",
            requestBody = Option(requestBody)
          )

          val result = connector.putFile(request).futureValue

          result mustBe Right(())
      }
    }
  }

  ".deleteFile" - {
    "should delete file on object store" in {
      forAll(alphaStringsWithMaxLength(12)) {
        fileId: String =>
          stub(
            DELETE,
            fileUploadEndpoint + "/" + fileId,
            Status.OK,
            responseBody = "some response",
            requestBody = None
          )

          val result = connector.deleteFile(fileId).futureValue

          result mustBe Right(())
      }
    }
  }
}
