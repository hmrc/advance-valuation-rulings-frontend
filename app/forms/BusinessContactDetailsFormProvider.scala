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

import scala.util.Try

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints

import com.google.i18n.phonenumbers.PhoneNumberUtil
import forms.BusinessContactDetailsFormProvider._
import forms.mappings.Mappings
import models.BusinessContactDetails

class BusinessContactDetailsFormProvider @Inject() extends Mappings {

  private val util                          = PhoneNumberUtil.getInstance
  def apply(): Form[BusinessContactDetails] =
    Form(
      mapping(
        "name"  -> text(nameRequiredError)
          .verifying(Constraints.pattern(Validation.nameInputPattern, error = nameFormatError))
          .verifying(maxLength(Validation.nameMaxLength, nameLengthError)),
        "email" -> text(emailRequiredError)
          .verifying(maxLength(Validation.emailMaxLength, emailLengthError))
          .verifying(Constraints.pattern(Validation.emailPattern, error = emailFormatError)),
        "phone" -> text(phoneRequiredError)
          .verifying(
            phoneFormatError,
            phone =>
              isValid(phone)
                && phone.length <= Validation.phoneNumberMaxLength
                && !phone.exists(_.isLetter)
          )
      )(BusinessContactDetails.apply)(
        (businessContactDetails: BusinessContactDetails) =>
          Some(
            (
              businessContactDetails.name,
              businessContactDetails.email,
              businessContactDetails.phone
            )
          )
      )
    )

  private def isValid(string: String): Boolean =
    Try(util.isPossibleNumber(util.parse(string, "GB")))
      .getOrElse(false)
}

object BusinessContactDetailsFormProvider {

  private val nameRequiredError = "businessContactDetails.fullName.error.required"
  private val nameFormatError   = "businessContactDetails.fullName.error.format"
  private val nameLengthError   = "businessContactDetails.fullName.length"

  private val emailRequiredError = "businessContactDetails.email.error.required"
  private val emailFormatError   = "businessContactDetails.email.error.format"
  private val emailLengthError   = "businessContactDetails.email.length"

  private val phoneRequiredError = "businessContactDetails.telephoneNumber.error.required"
  private val phoneFormatError   = "businessContactDetails.telephoneNumber.error.format"
}
