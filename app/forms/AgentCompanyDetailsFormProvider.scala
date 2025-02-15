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

package forms

import forms.mappings.Mappings
import models.{AgentCompanyDetails, Country}
import play.api.data.Form
import play.api.data.Forms._

class AgentCompanyDetailsFormProvider extends Mappings {

  def apply(): Form[AgentCompanyDetails] = Form(
    mapping(
      "agentEori"            -> text("agentCompanyDetails.error.agentEori.required")
        .verifying(eoriCode("agentCompanyDetails.error.agentEori.badLength")),
      "agentCompanyName"     -> text("agentCompanyDetails.error.agentCompanyName.required"),
      "agentStreetAndNumber" -> text("agentCompanyDetails.error.agentStreetAndNumber.required"),
      "agentCity"            -> text("agentCompanyDetails.error.agentCity.required"),
      "country"              -> text("agentCompanyDetails.error.agentCountry.required")
        .verifying(
          "agentCompanyDetails.error.agentCountry.required",
          x => Country.allCountries.exists(_.code == x)
        )
        .transform[Country](x => Country.allCountries.find(_.code == x).get, _.code),
      "agentPostalCode"      -> postcodeText(
        "agentCompanyDetails.error.agentPostalCode.required",
        "agentCompanyDetails.error.agentPostalCode.gb"
      )
    )(AgentCompanyDetails.apply)(o => Some(Tuple.fromProductTyped(o)))
  )
}
