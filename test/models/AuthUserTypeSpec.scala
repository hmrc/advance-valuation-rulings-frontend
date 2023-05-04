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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant, CredentialRole, User}

class AuthUserTypeSpec extends AnyFreeSpec with Matchers {

  "AuthUserType" - {

    "identifies an Invididual Trader" in {
      val result = AuthUserType(AffinityGroup.Individual, None)

      result mustBe Some(AuthUserType.IndividualTrader)
    }

    "identifies an Organisation Admin" in {
      val result = AuthUserType(AffinityGroup.Organisation, Some(Admin))

      result mustBe Some(AuthUserType.OrganisationAdmin)
    }

    "identifies an Organisation User" in {
      val result = AuthUserType(AffinityGroup.Organisation, Some(User))

      result mustBe Some(AuthUserType.OrganisationUser)
    }

    "identifies an Agent" in {
      val result = AuthUserType(AffinityGroup.Agent, None)

      result mustBe None
    }
  }
}
