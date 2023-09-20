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

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import play.api.http.Status
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import com.google.inject.Inject
import config.FrontendAppConfig
import models._
import models.requests._

@javax.inject.Singleton
class BackendConnector @Inject() (
  config: FrontendAppConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends FrontendHeaderCarrierProvider {

  type Result = Either[BackendError, TraderDetailsWithCountryCode]

  private val backendUrl = config.advanceValuationRulingsBackendURL

  def getTraderDetails(
    acknowledgementReference: AcknowledgementReference,
    eoriNumber: EoriNumber
  )(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[BackendError, TraderDetailsWithCountryCode]] =
    httpClient
      .get(url"$backendUrl/trader-details/${acknowledgementReference.value}/${eoriNumber.value}")
      .setHeader("X-Correlation-ID" -> UUID.randomUUID().toString)
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK =>
              Right(response.json.as[TraderDetailsWithCountryCode])
            case _  =>
              Left(BackendError(response.status, response.body))
          }
      }
      .recover {
        case e: Throwable =>
          onError(e)
      }

  def submitApplication(applicationRequest: ApplicationRequest)(implicit
    hc: HeaderCarrier
  ): Future[ApplicationSubmissionResponse] =
    httpClient
      .post(url"$backendUrl/applications")
      .withBody(Json.toJson(applicationRequest))
      .execute[ApplicationSubmissionResponse]

  def getApplication(
    applicationId: String
  )(implicit hc: HeaderCarrier): Future[Application] =
    httpClient
      .get(url"$backendUrl/applications/$applicationId")
      .execute[HttpResponse]
      .map {
        response =>
          val ret = response.json.as[Application]
          ret
      }

  def applicationSummaries(implicit hc: HeaderCarrier): Future[ApplicationSummaryResponse] =
    httpClient
      .get(url"$backendUrl/applications")
      .execute[ApplicationSummaryResponse]

  private def onError(ex: Throwable): Left[BackendError, Nothing] = {
    val (code, message) = ex match {
      case e: HttpException         => (e.responseCode, e.getMessage)
      case e: UpstreamErrorResponse => (e.reportAs, e.getMessage)
      case e: Throwable             => (Status.INTERNAL_SERVER_ERROR, e.getMessage)
    }
    Left(BackendError(code, message))
  }
}
