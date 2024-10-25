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

package models.events

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.User

class UserTypeEventSpec extends AnyFreeSpec with Matchers {
  
  val userTypeEvent: UserTypeEvent = UserTypeEvent(
    internalId = "internalId", 
    eori = "eori", 
    affinityGroup = Individual, 
    credentialRole = Some(User), 
    referrer = Some("String")
  )

  "An UserTypeEvent" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(userTypeEvent)
      json.validate[UserTypeEvent] mustEqual JsSuccess(userTypeEvent)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[UserTypeEvent].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      userTypeEvent mustEqual userTypeEvent
      userTypeEvent.hashCode mustEqual userTypeEvent.hashCode
    }

    "must have a working toString" in {
      userTypeEvent.toString must include("UserTypeEvent")
    }
  }
}
