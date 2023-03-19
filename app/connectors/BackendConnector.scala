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

import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}

import play.api.http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import com.google.inject.Inject
import config.FrontendAppConfig
import models.{AcknowledgementReference, BackendError, EoriNumber, TraderDetailsWithCountryCode, UserAnswers}
import models.fileupload.ReadyCallbackBody

class BackendConnector @Inject() (config: FrontendAppConfig, httpClient: HttpClient)
    extends FrontendHeaderCarrierProvider {
  import BackendConnector._

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
      .GET[TraderDetailsWithCountryCode](
        s"$backendUrl/trader-details/${acknowledgementReference.value}/${eoriNumber.value}",
        headers = Seq("X-Correlation-ID" -> UUID.randomUUID().toString)
      )
      .map(response => Right(response))
      .recover {
        case e: Throwable =>
          onError(e)
      }

  def submitAnswers(
    userAnswers: UserAnswers
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[BackendError, HttpResponse]] =
    httpClient
      .POST[UserAnswers, HttpResponse](
        s"$backendUrl/submit-answers",
        body = userAnswers,
        headers = Seq("X-Correlation-ID" -> UUID.randomUUID().toString)
      )
      .map {
        response =>
          if (Status.isSuccessful(response.status)) {
            response.asRight
          } else {
            BackendError(response.status, response.body).asLeft
          }
      }
      .recover {
        case e: Throwable =>
          onError(e)
      }

  def putFile(
    request: ObjectStorePutRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[BackendError, Unit]] =
    httpClient
      .POST[ObjectStorePutRequest, HttpResponse](
        s"$backendUrl/file",
        body = request,
        headers = Seq("X-Correlation-ID" -> UUID.randomUUID().toString)
      )
      .map {
        response =>
          if (Status.isSuccessful(response.status)) {
            ().asRight[BackendError]
          } else {
            BackendError(response.status, response.body).asLeft
          }
      }
      .recover {
        case e: Throwable =>
          onError(e)
      }

  def deleteFile(
    fileName: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[BackendError, Unit]] =
    httpClient
      .DELETE[HttpResponse](
        s"$backendUrl/file/$fileName",
        headers = Seq("X-Correlation-ID" -> UUID.randomUUID().toString)
      )
      .map {
        response =>
          if (Status.isSuccessful(response.status)) {
            ().asRight[BackendError]
          } else {
            BackendError(response.status, response.body).asLeft
          }
      }
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

object BackendConnector {
  import play.api.libs.json.{Json, OFormat}

  case class ObjectStorePutRequest(
    uploadId: String,
    downloadUrl: String,
    fileName: String,
    mimeType: String,
    size: Long,
    checksum: String
  )
  object ObjectStorePutRequest {
    implicit val format: OFormat[ObjectStorePutRequest] = Json.format[ObjectStorePutRequest]

    def apply(fileReadyCallback: ReadyCallbackBody): ObjectStorePutRequest =
      ObjectStorePutRequest(
        fileReadyCallback.reference.value,
        fileReadyCallback.downloadUrl.toString,
        fileReadyCallback.uploadDetails.fileName,
        fileReadyCallback.uploadDetails.fileMimeType,
        fileReadyCallback.uploadDetails.size,
        fileReadyCallback.uploadDetails.checksum
      )
  }
}
