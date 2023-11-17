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

import com.google.i18n.phonenumbers.PhoneNumberUtil
import forms.ApplicationContactDetailsFormProvider._
import forms.mappings.Mappings
import models.ApplicationContactDetails
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints

import javax.inject.Inject
import scala.util.Try

class ApplicationContactDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[ApplicationContactDetails] =
    Form(
      mapping(
        "name"     -> text(nameRequiredError)
          .verifying(
            Constraints.pattern(Validation.simpleCharactersInputPattern, error = nameFormatError)
          ),
        "email"    -> text(emailRequiredError)
          .verifying(Constraints.pattern(Validation.emailPattern, error = emailFormatError)),
        "phone"    -> text(phoneRequiredError)
          .verifying(
            phoneFormatError,
            phone =>
              isValid(phone) && !phone.exists(
                _.isLetter
              ) && phone.length <= Validation.phoneNumberMaxLength
          ),
        "jobTitle" -> text(jobTitleRequiredError)
          .verifying(
            Constraints
              .pattern(Validation.simpleCharactersInputPattern, error = jobTitleFormatError)
          )
      )(ApplicationContactDetails.apply)((applicationContactDetails: ApplicationContactDetails) =>
        Some(
          (
            applicationContactDetails.name,
            applicationContactDetails.email,
            applicationContactDetails.phone,
            applicationContactDetails.jobTitle
          )
        )
      )
    )
}

object ApplicationContactDetailsFormProvider {

  private val util = PhoneNumberUtil.getInstance

  private val nameRequiredError = "applicationContactDetails.fullName.error.required"
  private val nameFormatError   = "applicationContactDetails.simpleChars.error.format"

  private val emailRequiredError = "applicationContactDetails.email.error.required"
  private val emailFormatError   = "applicationContactDetails.email.error.format"

  private val phoneRequiredError = "applicationContactDetails.telephoneNumber.error.required"
  private val phoneFormatError   = "applicationContactDetails.telephoneNumber.error.format"

  private val jobTitleRequiredError = "applicationContactDetails.jobTitle.error.required"
  private val jobTitleFormatError   = "applicationContactDetails.simpleChars.error.format"

  private def isValid(string: String): Boolean =
    Try(util.isPossibleNumber(util.parse(string, "GB")))
      .getOrElse(false)
}
