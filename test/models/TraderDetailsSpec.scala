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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

class TraderDetailsSpec extends AnyFreeSpec with Matchers {

  val traderDetails: TraderDetails = TraderDetails(
    eori = "eori",
    name = "name",
    streetAndNumber = "streetAndNumber",
    city = "city",
    country = Some("GB"),
    postalCode = Some("postcode")
  )

  "A TraderDetails" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(traderDetails)
      json.validate[TraderDetails] mustEqual JsSuccess(traderDetails)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[TraderDetails].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      traderDetails mustEqual traderDetails
      traderDetails.hashCode mustEqual traderDetails.hashCode
    }

    "must have a working toString" in {
      traderDetails.toString must include("TraderDetails")
    }
  }
}
