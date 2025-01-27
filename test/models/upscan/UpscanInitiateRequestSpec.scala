/*
 * Copyright 2025 HM Revenue & Customs
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

package models.upscan

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class UpscanInitiateRequestSpec extends AnyFreeSpec with Matchers {

  val upscanInitiateRequest: UpscanInitiateRequest = UpscanInitiateRequest(
    callbackUrl = "s3://bucket/callbackUrl",
    successRedirect = "s3://bucket/successRedirect",
    errorRedirect = "s3://bucket/errorRedirect",
    minimumFileSize = 123,
    maximumFileSize = 321
  )

  "An UpscanInitiateRequest" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(upscanInitiateRequest)
      json.validate[UpscanInitiateRequest] mustEqual JsSuccess(upscanInitiateRequest)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[UpscanInitiateRequest].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      upscanInitiateRequest mustEqual upscanInitiateRequest
      upscanInitiateRequest.hashCode mustEqual upscanInitiateRequest.hashCode
    }

    "must have a working toString" in {
      upscanInitiateRequest.toString must include("UpscanInitiateRequest")
    }
  }
}
