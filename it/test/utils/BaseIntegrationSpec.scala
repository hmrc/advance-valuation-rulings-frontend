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

package utils

import org.apache.pekko.actor.ActorSystem
import config.FrontendAppConfig
import models.{AcknowledgementReference, EoriNumber}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, EitherValues}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import scala.concurrent.ExecutionContext

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
  val applicationEndpoint   = "/advance-valuation-rulings/application"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure("metrics.enabled" -> false)
      .configure("auditing.enabled" -> false)
      .configure("create-internal-auth-token-on-start" -> false)
      .configure("internal-auth.token" -> "authToken")
      .configure(
        "microservice.services.advance-valuation-rulings-backend.port" -> WireMockHelper.wireMockPort
      )
      .configure(
        "microservice.services.email.port" -> WireMockHelper.wireMockPort
      )
      .build()

  implicit lazy val ec: ExecutionContext = fakeApplication().injector.instanceOf[ExecutionContext]
  lazy val httpClient: HttpClientV2      = fakeApplication().injector.instanceOf[HttpClientV2]
  lazy val appConfig: FrontendAppConfig  = fakeApplication().injector.instanceOf[FrontendAppConfig]

  def traderDetailsRequestUrl(
    acknowledgementReference: AcknowledgementReference,
    eoriNumber: EoriNumber
  ): String =
    s"$traderDetailsEndpoint/${URLEncoder.encode(acknowledgementReference.value, StandardCharsets.UTF_8.displayName())}/${URLEncoder
        .encode(eoriNumber.value, StandardCharsets.UTF_8.displayName())}"

  def getApplicationRequestUrl(
    id: String
  ): String =
    s"$applicationEndpoint/${URLEncoder.encode(id, StandardCharsets.UTF_8.displayName())}"
}
