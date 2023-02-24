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

import scala.concurrent.{ExecutionContext, Future}

//import play.api.Logger
import play.api.http.Status
import play.api.mvc.Request
import uk.gov.hmrc.http.{HttpClient, HttpException, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import com.google.inject.Inject
import models.{BackendError, TraderDetails}
import models.requests.TraderDetailsRequest

class BackendConnector @Inject() (
  servicesConfig: ServicesConfig,
  httpClient: HttpClient
)(implicit
  ec: ExecutionContext
) extends FrontendHeaderCarrierProvider {

  type Result = Either[BackendError, TraderDetails]

  private val backendBaseUrl = servicesConfig.baseUrl("advance-valuation-rulings-backend")
  private val backendURL     = s"$backendBaseUrl/advance-valuation-rulings"

  def getTraderDetails(
    traderDetailsRequest: TraderDetailsRequest
  )(implicit request: Request[_]): Future[Either[BackendError, TraderDetails]] =
    httpClient
      .POST[TraderDetailsRequest, TraderDetails](
        s"$backendURL/trader-details",
        traderDetailsRequest
      )
      .map(response => Right(response))
      .recover {
        case e: Throwable =>
          onError(e)
      }

  private def onError(ex: Throwable): Left[BackendError, Nothing] = {
    val (code, message) = ex match {
      case e: HttpException         => (e.responseCode, e.getMessage)
      case e: UpstreamErrorResponse => (e.reportAs, e.getMessage)
      case e: Throwable             => (Status.INTERNAL_SERVER_ERROR, e.getMessage)
    }
    Left(BackendError(code, message))
  }
}
