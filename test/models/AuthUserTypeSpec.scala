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

package models

import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup, Assistant, User}

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class AuthUserTypeSpec extends AnyFreeSpec with Matchers with TableDrivenPropertyChecks {

  "AuthUserType" - {

    "identifies an Invididual Trader" in {
      val result = AuthUserType(AffinityGroup.Individual, None)

      result mustBe Some(AuthUserType.IndividualTrader)
    }

    "identifies an Organisation Assistant" in {
      val result = AuthUserType(AffinityGroup.Organisation, Some(Assistant))

      result mustBe Some(AuthUserType.OrganisationAssistant)
    }

    "identifies an Organisation Admin with the Admin Credential Role" in {
      val result = AuthUserType(AffinityGroup.Organisation, Some(Admin))

      result mustBe Some(AuthUserType.OrganisationAdmin)
    }

    "identifies an Organisation Admin with the User Credential Role" in {
      val result = AuthUserType(AffinityGroup.Organisation, Some(User))

      result mustBe Some(AuthUserType.OrganisationAdmin)
    }

    "identifies an Agent" in {
      val result = AuthUserType(AffinityGroup.Agent, None)

      result mustBe None
    }

    val scenarios = Table(
      ("authUserType", "expectedJson"),
      (AuthUserType.IndividualTrader, JsString("IndividualTrader")),
      (AuthUserType.OrganisationAssistant, JsString("OrganisationAssistant")),
      (AuthUserType.OrganisationAdmin, JsString("OrganisationAdmin"))
    )

    "serialises to Json" in {
      forAll(scenarios) {
        (authUserType, expectedJson) => Json.toJson(authUserType) mustEqual expectedJson
      }
    }

    "deserialises from Json" in {
      forAll(scenarios) {
        (authUserType, expectedJson) =>
          Json.fromJson[AuthUserType](expectedJson) mustEqual JsSuccess(authUserType)
      }
    }
  }
}
