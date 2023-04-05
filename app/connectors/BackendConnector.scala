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

import java.time.Instant
import java.util.UUID

import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}

import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps, UpstreamErrorResponse}
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

  private var db: Map[String, Application] = Map.empty

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
      .execute[TraderDetailsWithCountryCode]
      .map(response => Right(response))
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
  ): Future[Either[BackendError, Application]] =
    db.get(applicationId) match {
      case Some(application) =>
        Future.successful(
          Right(
            application
          )
        )
      case None              =>
        Future.successful(
          Left(
            BackendError(
              Status.NOT_FOUND,
              s"Application with id $applicationId not found"
            )
          )
        )
    }

  def applicationSummaries(
    request: ApplicationSummaryRequest
  )(implicit
    ec: ExecutionContext
  ): Future[Either[BackendError, ApplicationSummaryResponse]] =
    db.values
      .foldLeft(ApplicationSummaryResponse(Seq.empty)) {
        case (acc, application) =>
          if (application.request.trader.eori == request.eoriNumber) {
            val summary = models.requests.ApplicationSummary(
              id = application.id,
              goodsName = application.request.goodsDetails.goodsName,
              dateSubmitted = application.created,
              eoriNumber = application.request.trader.eori
            )
            acc.copy(summaries = acc.summaries :+ summary)
          } else {
            acc
          }
      }
      .asRight[BackendError]
      .pure[Future]

  private def onError(ex: Throwable): Left[BackendError, Nothing] = {
    val (code, message) = ex match {
      case e: HttpException         => (e.responseCode, e.getMessage)
      case e: UpstreamErrorResponse => (e.reportAs, e.getMessage)
      case e: Throwable             => (Status.INTERNAL_SERVER_ERROR, e.getMessage)
    }
    Left(BackendError(code, message))
  }
}
