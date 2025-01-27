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
import org.scalatest.matchers.must.Matchers.{must, mustBe, mustEqual}
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsObject, Json}

class BackendErrorSpec extends AnyFreeSpec with Matchers {

  private val backendError: BackendError = BackendError(
    code = 200,
    message = "Error"
  )

  private val json = Json.obj(
    "code"    -> 200,
    "message" -> "Error"
  )

  "A BackendError" - {
    "serialise to JSON" in {
      Json.toJson(backendError) mustBe json
    }

    "deserialise from JSON" in {
      json.as[BackendError] mustBe backendError
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[BackendError].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      backendError mustEqual backendError
      backendError.hashCode mustEqual backendError.hashCode
    }

    "must have a working toString" in {
      backendError.toString must include("BackendError")
    }
  }

}
