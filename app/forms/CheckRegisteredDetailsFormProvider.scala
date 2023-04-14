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
import uk.gov.hmrc.auth.core.AffinityGroup

import forms.mappings.Mappings

class CheckRegisteredDetailsFormProvider @Inject() extends Mappings {

  def apply(
    affinityGroup: AffinityGroup,
    consentToDisclosureOfPersonalData: Boolean
  ): Form[Boolean] =
    (consentToDisclosureOfPersonalData, affinityGroup) match {
      case (false, _)                         =>
        Form(
          "value" -> boolean("checkRegisteredDetails.error.required.consent")
        )
      case (true, AffinityGroup.Organisation) =>
        Form(
          "value" -> boolean("checkRegisteredDetails.error.required.organisation")
        )
      case (true, AffinityGroup.Individual)   =>
        Form(
          "value" -> boolean("checkRegisteredDetails.error.required.individual")
        )
      case (true, AffinityGroup.Agent)        =>
        Form(
          "value" -> boolean("checkRegisteredDetails.error.required.agent")
        )
      case _                                  => throw new IllegalArgumentException("Affinity group not supported")
    }

}
