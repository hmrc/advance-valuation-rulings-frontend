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

package forms

import javax.inject.Inject

import play.api.data.Form
import play.api.data.Forms._

import forms.mappings.Mappings
import models.AgentCompanyDetails

class AgentCompanyDetailsFormProvider @Inject() extends Mappings {

  private val maximumValueForAddress    = 70
  private val maximumValueForTownOrCity = 35

  // TODO: Make Postcode mandatory
  def apply(): Form[AgentCompanyDetails] = Form(
    mapping(
      "agentEori"            -> text("agentCompanyDetails.error.agentEori.required")
        .verifying(eoriCodeConstraint),
      "agentCompanyName"     -> text("agentCompanyDetails.error.agentCompanyName.required"),
      "agentStreetAndNumber" -> text("agentCompanyDetails.error.agentStreetAndNumber.required")
        .verifying(
          maxLength(maximumValueForAddress, "agentCompanyDetails.error.agentStreetAndNumber.length")
        ),
      "agentCity"            -> text("agentCompanyDetails.error.agentCity.required")
        .verifying(
          maxLength(maximumValueForTownOrCity, "agentCompanyDetails.error.agentCity.length")
        ),
      "agentCountry"         -> text("agentCompanyDetails.error.agentCountry.required"),
      "agentPostalCode"      -> postcodeText(
        "agentCompanyDetails.error.agentPostalCode.required",
        "agentCompanyDetails.error.agentPostalCode.gb"
      )
        .verifying(optionalPostCodeMaxLength("agentCompanyDetails.error.agentPostalCode.length"))
    )(AgentCompanyDetails.apply)(AgentCompanyDetails.unapply)
  )
}
