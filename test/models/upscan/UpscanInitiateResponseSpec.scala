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

package models.upscan

import models.upscan.UpscanInitiateResponse.UploadRequest
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class UpscanInitiateResponseSpec extends AnyFreeSpec with Matchers {

  val uploadRequest: UploadRequest = UploadRequest(
    href = "href",
    fields = Map("field1" -> "field2")
  )

  "An UploadRequest" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(uploadRequest)
      json.validate[UploadRequest] mustEqual JsSuccess(uploadRequest)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[UploadRequest].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      uploadRequest mustEqual uploadRequest
      uploadRequest.hashCode mustEqual uploadRequest.hashCode
    }

    "must have a working toString" in {
      uploadRequest.toString must include("UploadRequest")
    }
  }
}
