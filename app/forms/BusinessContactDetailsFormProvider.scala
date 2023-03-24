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
import play.api.data.validation.Constraints

import forms.BusinessContactDetailsFormProvider._
import forms.mappings.Mappings
import models.BusinessContactDetails

class BusinessContactDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BusinessContactDetails] =
    Form(
      mapping(
        "name"    -> text(nameRequiredError)
          .verifying(Constraints.pattern(nameRegex, error = nameFormatError))
          .verifying(maxLength(nameMaxLength, nameLengthError)),
        "email"   -> text(emailRequiredError)
          .verifying(Constraints.pattern(emailRegex, error = emailFormatError)),
        "phone"   -> text(phoneRequiredError)
          .verifying(Constraints.pattern(phoneNumberRegex, error = phoneFormatError))
          .verifying(maxLength(phoneNumberMaxLength, phoneLengthError)),
        "company" -> text(companyRequiredError)
      )(BusinessContactDetails.apply)(
        (businessContactDetails: BusinessContactDetails) =>
          Some(
            (
              businessContactDetails.name,
              businessContactDetails.email,
              businessContactDetails.phone,
              businessContactDetails.company
            )
          )
      )
    )
}

object BusinessContactDetailsFormProvider {

  private val nameMaxLength = 100
  private val nameRegex     = "^[a-zA-Z -]*$".r

  private val phoneNumberMaxLength = 24
  private val phoneNumberRegex     = "^[0-9]*$".r

  private val emailRegex = """[^”r-v\[ ]*+[^ ]++|(?<= )[a-zA-Z]@\w[^ ]++""".r

  private val nameRequiredError = "businessContactDetails.fullName.error.required"
  private val nameFormatError   = "businessContactDetails.fullName.error.format"
  private val nameLengthError   = "businessContactDetails.fullName.length"

  private val emailRequiredError = "businessContactDetails.email.error.required"
  private val emailFormatError   = "businessContactDetails.email.error.format"

  private val phoneRequiredError = "businessContactDetails.telephoneNumber.error.required"
  private val phoneFormatError   = "businessContactDetails.telephoneNumber.error.format"
  private val phoneLengthError   = "businessContactDetails.telephoneNumber.length"

  private val companyRequiredError = "businessContactDetails.companyName.error.required"
}
