package connectors

import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import utils.WireMockHelper

class UpscanConnectorSpec
    extends AnyFreeSpec
    with Matchers
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

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

  private lazy val app = GuiceApplicationBuilder()
    .configure(
      "appName"                                    -> "app",
      "microservice.services.upscan-initiate.port" -> wireMockServer.port()
    )
    .build()

  private lazy val connector = app.injector.instanceOf[UpscanConnector]

  private val request = UpscanConnector.UpscanInitiateRequest(
    callbackUrl = "someCallback",
    successRedirect = "successRedirect",
    errorRedirect = "errorRedirect",
    minimumFileSize = 123,
    maximumFileSize = 321
  )

  private val response = UpscanConnector.UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanConnector.UpscanInitiateResponse.UploadRequest(
      href = "foobar",
      fields = Map("foo" -> "bar")
    )
  )

  private val hc = HeaderCarrier()

  "initiate" - {

    "must return an UpscanInitiateResponse" in {

      wireMockServer.stubFor(
        post(urlPathEqualTo("/upscan/v2/initiate"))
          .withHeader("User-Agent", equalTo("app"))
          .withRequestBody(equalToJson(Json.toJson(request).toString))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(Json.toJson(response).toString)
          )
      )

      connector.initiate(request)(hc).futureValue mustEqual response
    }

    "must fail when the server responds with an error" in {

      wireMockServer.stubFor(
        post(urlPathEqualTo("/upscan/v2/initiate"))
          .withHeader("User-Agent", equalTo("app"))
          .willReturn(
            aResponse().withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      connector.initiate(request)(hc).failed.futureValue
    }
  }
}
