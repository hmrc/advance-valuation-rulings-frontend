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

package controllers.common

import java.time.Instant

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.Logger
import play.api.libs.json.JsObject
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import base.SpecBase
import connectors.BackendConnector
import models.{BackendError, DraftId, TraderDetailsWithCountryCode, UserAnswers}
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary
import org.scalatestplus.mockito.MockitoSugar

class TraderDetailsHelperSpec extends SpecBase with MockitoSugar {

  private class Harness extends TraderDetailsHelper {}

  implicit val mockConnector = mock[BackendConnector]
  implicit val logger        = Logger("test")

  private def buildRequest(): DataRequest[AnyContent] = {
    val userId  = "userId"
    val eori    = "eori"
    val draftId = DraftId(Arbitrary.arbitrary[Long].sample.get)
    DataRequest(
      FakeRequest(GET, ""),
      userId,
      eori,
      UserAnswers(userId, draftId, JsObject.empty, Instant.now),
      AffinityGroup.Individual,
      None
    )
  }

  implicit val request = buildRequest()
  implicit val hc      = HeaderCarrierConverter.fromRequest(request)

  "getTraderDetails" - {
    "returns success logic when connector returns details" in {

      when(mockConnector.getTraderDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(traderDetailsWithCountryCode)))

      val sut    = new Harness()
      val result = sut.getTraderDetails(_ => Future.successful(Ok("test")))

      status(result) mustEqual OK
    }

    "returns redirect to JourneyRecoveryController when non-404 response" in {
      when(mockConnector.getTraderDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(BackendError(500, "test"))))

      val sut    = new Harness()
      val result = sut.getTraderDetails(_ => Future.successful(Ok("test")))

      status(result) mustEqual SEE_OTHER
    }

    "returns redirect to JourneyRecoveryController when 404 response but notFound is a None" in {
      when(mockConnector.getTraderDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(BackendError(404, "test"))))

      val sut    = new Harness()
      val result = sut.getTraderDetails(_ => Future.successful(Ok("test")))

      status(result) mustEqual SEE_OTHER
    }

    "returns redirect to notFound result when 404 response and notFound provided" in {
      when(mockConnector.getTraderDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(BackendError(404, "test"))))

      val sut    = new Harness()
      val result = sut.getTraderDetails(
        _ => Future.successful(Ok("test")),
        Some(Future.successful(NotFound("not found")))
      )

      status(result) mustEqual NOT_FOUND
    }
  }
}