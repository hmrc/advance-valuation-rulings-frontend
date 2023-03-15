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

import forms.ApplicationContactDetailsFormProvider._
import forms.mappings.Mappings
import models.ApplicationContactDetails

class ApplicationContactDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[ApplicationContactDetails] =
    Form(
      mapping(
        "name"  -> text(nameRequiredError)
          .verifying(Constraints.pattern(nameRegex, error = nameFormatError))
          .verifying(maxLength(nameMaxLength, nameLengthError)),
        "email" -> text(emailRequiredError)
          .verifying(Constraints.pattern(emailRegex, error = emailFormatError)),
        "phone" -> text(phoneRequiredError)
          .verifying(Constraints.pattern(phoneNumberRegex, error = phoneFormatError))
          .verifying(maxLength(phoneNumberMaxLength, phoneLengthError))
      )(ApplicationContactDetails.apply)(
        (applicationContactDetails: ApplicationContactDetails) =>
          Some(
            (
              applicationContactDetails.name,
              applicationContactDetails.email,
              applicationContactDetails.phone
            )
          )
      )
    )
}

object ApplicationContactDetailsFormProvider {

  private val nameMaxLength = 100
  private val nameRegex     = "^[a-zA-Z -]*$".r

  private val phoneNumberMaxLength = 24
  private val phoneNumberRegex     = "^[0-9]*$".r

  private val emailRegex = """[^”r-v\[ ]*+[^ ]++|(?<= )[a-zA-Z]@\w[^ ]++""".r

  private val nameRequiredError = "applicationContactDetails.fullName.error.required"
  private val nameFormatError   = "applicationContactDetails.fullName.error.format"
  private val nameLengthError   = "applicationContactDetails.fullName.length"

  private val emailRequiredError = "applicationContactDetails.email.error.required"
  private val emailFormatError   = "applicationContactDetails.email.error.format"

  private val phoneRequiredError = "applicationContactDetails.telephoneNumber.error.required"
  private val phoneFormatError   = "applicationContactDetails.telephoneNumber.error.format"
  private val phoneLengthError   = "applicationContactDetails.telephoneNumber.length"
}
