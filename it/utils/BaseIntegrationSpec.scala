package utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import scala.concurrent.ExecutionContext

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import akka.actor.ActorSystem
import config.FrontendAppConfig
import models.{AcknowledgementReference, EoriNumber}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, EitherValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait BaseIntegrationSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with EitherValues
    with GuiceOneServerPerSuite
    with BeforeAndAfterAll
    with TableDrivenPropertyChecks
    with ScalaCheckPropertyChecks
    with BeforeAndAfterEach {

  implicit val system: ActorSystem               = ActorSystem()
  implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrier()

  val traderDetailsEndpoint = "/advance-valuation-rulings/trader-details"
  val submitAnswersEndpoint = "/advance-valuation-rulings/submit-answers"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .configure("auditing.enabled" -> false)
      .configure(
        "microservice.services.advance-valuation-rulings-backend.port" -> WireMockHelper.wireMockPort
      )
      .build()

  implicit lazy val ec: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]
  lazy val httpClient: DefaultHttpClient = fakeApplication().injector.instanceOf[DefaultHttpClient]
  lazy val appConfig: FrontendAppConfig  = fakeApplication().injector.instanceOf[FrontendAppConfig]

  def traderDetailsRequestUrl(
    acknowledgementReference: AcknowledgementReference,
    eoriNumber: EoriNumber
  ): String =
    s"$traderDetailsEndpoint/${URLEncoder.encode(acknowledgementReference.value, StandardCharsets.UTF_8.displayName())}/${URLEncoder
        .encode(eoriNumber.value, StandardCharsets.UTF_8.displayName())}"
}
