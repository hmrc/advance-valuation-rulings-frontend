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

import scala.concurrent.Future

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpException, HttpResponse, UpstreamErrorResponse}

import base.SpecBase
import org.scalatest.concurrent.ScalaFutures

class HttpClientResponseSpec extends SpecBase with ScalaFutures {

  "httpClientResponse" - {
    val httpClientResponse: HttpClientResponse = inject[HttpClientResponse]

    "read" - {
      "pass response through for a Right" in {
        val response = Future.successful(Right(HttpResponse(OK, "response")))
        val result   = httpClientResponse.read(response).value.futureValue

        result mustBe a[Right[_, _]]
        result.getOrElse(HttpResponse(IM_A_TEAPOT, "")) mustBe HttpResponse(
          OK,
          "response",
          _: Map[String, Seq[String]]
        )
      }

      val dummyUpstreamError: UpstreamErrorResponse =
        UpstreamErrorResponse("", IM_A_TEAPOT, IM_A_TEAPOT)

      "pass response through for a Left (NOT_FOUND)" in {
        val response =
          Future.successful(Left(UpstreamErrorResponse("response", NOT_FOUND, BAD_GATEWAY)))
        val result   = httpClientResponse.read(response).value.futureValue

        result mustBe a[Left[_, _]]

        result.swap.getOrElse(dummyUpstreamError) mustBe
          UpstreamErrorResponse("response", BAD_REQUEST, BAD_GATEWAY, _: Map[String, Seq[String]])
      }

      "pass response through for a Left (INTERNAL_SERVER_ERROR)" in {
        val response =
          Future.successful(
            Left(UpstreamErrorResponse("response", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))
          )
        val result   = httpClientResponse.read(response).value.futureValue

        result mustBe a[Left[_, _]]

        result.swap.getOrElse(dummyUpstreamError) mustBe
          UpstreamErrorResponse("response", BAD_REQUEST, BAD_GATEWAY, _: Map[String, Seq[String]])
      }

      "pass response through for a Left (BAD_REQUEST)" in {
        val response =
          Future.successful(Left(UpstreamErrorResponse("response", BAD_REQUEST, BAD_GATEWAY)))
        val result   = httpClientResponse.read(response).value.futureValue

        result mustBe a[Left[_, _]]

        result.swap.getOrElse(dummyUpstreamError) mustBe
          UpstreamErrorResponse("response", BAD_REQUEST, BAD_GATEWAY, _: Map[String, Seq[String]])
      }

      "deal with HttpExceptions" in {
        val response = Future.failed(new HttpException("someError", BAD_REQUEST))
        val result   = httpClientResponse.read(response).value.futureValue

        result mustBe a[Left[_, _]]
        result.swap.getOrElse(dummyUpstreamError) mustBe
          UpstreamErrorResponse("someError", BAD_GATEWAY, BAD_GATEWAY, _: Map[String, Seq[String]])
      }

      "deal with other exceptions" in {
        val response = Future.failed(new RuntimeException("someError"))

        intercept[RuntimeException](
          httpClientResponse.read(response).value.futureValue
        )
      }
    }
  }
}
