package connectors

import java.time.Instant

import play.api.libs.json.Json

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{DraftId, UserAnswers}
import org.scalatest.OptionValues
import utils.{BaseIntegrationSpec, WireMockHelper}

class UserAnswersConnectorSpec extends BaseIntegrationSpec with WireMockHelper with OptionValues {

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

  private val connector = new UserAnswersConnector(appConfig, httpClient)

  private val draftId = DraftId(0)
  private val answers = UserAnswers(
    userId = "userId",
    draftId = draftId,
    data = Json.obj(),
    lastUpdated = Instant.now
  )

  ".set" - {

    "must send user answers to the backend" in {

      wireMockServer.stubFor(
        post(urlEqualTo("/advance-valuation-rulings/user-answers"))
          .willReturn(noContent())
      )

      connector.set(answers).futureValue
    }

    "must return a failed future when the server returns an error" in {

      wireMockServer.stubFor(
        post(urlEqualTo("/advance-valuation-rulings/user-answers"))
          .willReturn(serverError())
      )

      connector.set(answers).failed.futureValue
    }
  }

  ".get" - {

    "must return user answers when the server provides them" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId"))
          .willReturn(ok(Json.toJson(answers).toString))
      )

      val result = connector.get(draftId).futureValue
      result.value mustEqual answers
    }

    "must return None when the server responds with Not Found" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId"))
          .willReturn(notFound())
      )

      val result = connector.get(draftId).futureValue
      result must not be defined
    }

    "must return a failed future when the server returns an error" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId"))
          .willReturn(serverError())
      )

      connector.get(draftId).failed.futureValue
    }
  }

  ".clear" - {

    "must succeed when the server responds with No Content" in {

      wireMockServer.stubFor(
        delete(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId"))
          .willReturn(noContent())
      )

      connector.clear(draftId).futureValue
    }

    "must return a failed future when the server returns an error" in {

      wireMockServer.stubFor(
        delete(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId"))
          .willReturn(serverError())
      )

      connector.clear(draftId).failed.futureValue
    }
  }

  ".keepAlive" - {

    "must succeed when the server responds with No Content" in {

      wireMockServer.stubFor(
        post(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId/keep-alive"))
          .willReturn(noContent())
      )

      connector.keepAlive(draftId).futureValue
    }

    "must return a failed future when the server returns an error" in {

      wireMockServer.stubFor(
        post(urlEqualTo(s"/advance-valuation-rulings/user-answers/$draftId/keep-alive"))
          .willReturn(serverError())
      )

      connector.keepAlive(draftId).failed.futureValue
    }
  }
}
