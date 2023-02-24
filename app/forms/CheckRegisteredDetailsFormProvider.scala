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
import play.api.data.Forms.mapping

import forms.mappings.Mappings
import models.CheckRegisteredDetails

class CheckRegisteredDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[CheckRegisteredDetails] =
    Form(
      mapping(
        "value"           -> boolean("checkRegisteredDetails.error.required"),
        "eori"            -> text("checkRegisteredDetails.eori.error.required"),
        "name"            -> text("checkRegisteredDetails.name.error.required"),
        "streetAndNumber" -> text("checkRegisteredDetails.streetAndNumber.error.required"),
        "city"            -> text("checkRegisteredDetails.city.error.required"),
        "country"         -> text("checkRegisteredDetails.country.error.required"),
        "postalCode"      -> text("checkRegisteredDetails.postalCode.error.required")
      )(CheckRegisteredDetails.apply)(
        (checkRegisteredDetails: CheckRegisteredDetails) =>
          Option(
            (
              checkRegisteredDetails.value,
              checkRegisteredDetails.eori,
              checkRegisteredDetails.name,
              checkRegisteredDetails.streetAndNumber,
              checkRegisteredDetails.city,
              checkRegisteredDetails.country,
              checkRegisteredDetails.postalCode
            )
          )
      )
    )
}
