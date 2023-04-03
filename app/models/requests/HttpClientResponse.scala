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

package models.requests

import cats.data.EitherT
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpException, HttpResponse, UpstreamErrorResponse}

import com.google.inject.Inject

class HttpClientResponse @Inject() ()(implicit ec: ExecutionContext) extends Logging {

  def read(
    response: Future[Either[UpstreamErrorResponse, HttpResponse]]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    EitherT(response.map {
      case Right(response) => Right(response)
      case Left(error)     =>
        if (error.statusCode == NOT_FOUND || error.statusCode == UNPROCESSABLE_ENTITY) {
          logger.info(error.message)
        } else if (error.statusCode >= 499 || error.statusCode == TOO_MANY_REQUESTS) {
          logger.error(error.message)
        } else {
          logger.error(error.message, error)
        }
        Left(error)
    } recover {
      case exception: HttpException =>
        logger.error(exception.message)
        Left(UpstreamErrorResponse(exception.message, BAD_GATEWAY, BAD_GATEWAY))
      case exception: Exception     => throw exception
    })
}
