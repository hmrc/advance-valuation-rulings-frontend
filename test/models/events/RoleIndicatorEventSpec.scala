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

import models.WhatIsYourRoleAsImporter.EmployeeOfOrg
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.User

class RoleIndicatorEventSpec extends AnyFreeSpec with Matchers {
  
  val roleIndicatorEvent: RoleIndicatorEvent = RoleIndicatorEvent(
    internalId = "internalId",
    eori = "eori",
    affinityGroup = Individual,
    credentialRole = Some(User),
    role = EmployeeOfOrg
  )

  "A RoleIndicatorEvent" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(roleIndicatorEvent)
      json.validate[RoleIndicatorEvent] mustEqual JsSuccess(roleIndicatorEvent)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[RoleIndicatorEvent].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      roleIndicatorEvent mustEqual roleIndicatorEvent
      roleIndicatorEvent.hashCode mustEqual roleIndicatorEvent.hashCode
    }

    "must have a working toString" in {
      roleIndicatorEvent.toString must include("RoleIndicatorEvent")
    }
  }
}
