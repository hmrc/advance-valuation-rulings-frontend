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

package models.requests

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.time.Instant

class ApplicationSummarySpec extends AnyFreeSpec with Matchers {

  val applicationSummary: ApplicationSummary = ApplicationSummary(
    id = ApplicationId(1),
    goodsDescription = "Goods Description",
    dateSubmitted = Instant.now,
    eoriNumber = "eori"
  )

  val applicationSummaryRequest: ApplicationSummaryRequest = ApplicationSummaryRequest(
    eoriNumber = "eori"
  )
  
  val applicationSummaryResponse: ApplicationSummaryResponse = ApplicationSummaryResponse(
    summaries = Seq(ApplicationSummary(ApplicationId(1), "Goods Description", Instant.now, "eori"))
  )
  
  "An ApplicationSummary" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(applicationSummary)
      json.validate[ApplicationSummary] mustEqual JsSuccess(applicationSummary)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[ApplicationSummary].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      applicationSummary mustEqual applicationSummary
      applicationSummary.hashCode mustEqual applicationSummary.hashCode
    }

    "must have a working toString" in {
      applicationSummary.toString must include("ApplicationSummary")
    }
  }

  "An ApplicationSummaryRequest" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(applicationSummaryRequest)
      json.validate[ApplicationSummaryRequest] mustEqual JsSuccess(applicationSummaryRequest)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[ApplicationSummaryRequest].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      applicationSummaryRequest mustEqual applicationSummaryRequest
      applicationSummaryRequest.hashCode mustEqual applicationSummaryRequest.hashCode
    }

    "must have a working toString" in {
      applicationSummaryRequest.toString must include("ApplicationSummaryRequest")
    }
  }

  "An ApplicationSummaryResponse" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(applicationSummaryResponse)
      json.validate[ApplicationSummaryResponse] mustEqual JsSuccess(applicationSummaryResponse)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[ApplicationSummaryResponse].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      applicationSummaryResponse mustEqual applicationSummaryResponse
      applicationSummaryResponse.hashCode mustEqual applicationSummaryResponse.hashCode
    }

    "must have a working toString" in {
      applicationSummaryResponse.toString must include("ApplicationSummaryResponse")
    }
  }
}
