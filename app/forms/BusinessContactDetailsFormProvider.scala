/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.BusinessContactDetailsFormProvider._
import forms.mappings.Mappings
import models.BusinessContactDetails
import play.api.data.Forms._
import play.api.data.validation.Constraints
import play.api.data.{Form, Mapping}

import scala.util.Try

class BusinessContactDetailsFormProvider extends Mappings {

  private val util = PhoneNumberUtil.getInstance

  private val nameMapping: (String, Mapping[String]) = "name" -> text(nameRequiredError)
    .verifying(
      Constraints.pattern(Validation.simpleCharactersInputPattern, error = nameFormatError)
    )

  private val emailMapping: (String, Mapping[String]) = "email" -> text(emailRequiredError)
    .verifying(Constraints.pattern(Validation.emailPattern, error = emailFormatError))

  private val phoneMapping: (String, Mapping[String]) = "phone" -> text(phoneRequiredError)
    .verifying(
      phoneFormatError,
      phone =>
        isValid(phone) && !phone.exists(
          _.isLetter
        ) && phone.length <= Validation.phoneNumberMaxLength
    )

  private val companyNameMapping: (String, Mapping[String]) =
    "companyName" -> text(companyNameRequiredError)

  private val jobTitleMapping: (String, Mapping[String]) = "jobTitle" -> text(jobTitleRequiredError)
    .verifying(
      Constraints.pattern(Validation.simpleCharactersInputPattern, error = jobTitleFormatError)
    )

  private val defaultMap =
    mapping(nameMapping, emailMapping, phoneMapping, jobTitleMapping)((name, email, phone, jobTitle) =>
      BusinessContactDetails.apply(name, email, phone, None, jobTitle)
    )((businessContactDetails: BusinessContactDetails) =>
      Some(
        (
          businessContactDetails.name,
          businessContactDetails.email,
          businessContactDetails.phone,
          businessContactDetails.jobTitle
        )
      )
    )

  private val companyNameIncMap =
    mapping(nameMapping, emailMapping, phoneMapping, companyNameMapping, jobTitleMapping)(
      (name, email, phone, companyName, jobTitle) =>
        BusinessContactDetails.apply(name, email, phone, Some(companyName), jobTitle)
    )((businessContactDetails: BusinessContactDetails) =>
      Some(
        (
          businessContactDetails.name,
          businessContactDetails.email,
          businessContactDetails.phone,
          businessContactDetails.companyName.getOrElse(""),
          businessContactDetails.jobTitle
        )
      )
    )

  def apply(includeCompanyName: Boolean): Form[BusinessContactDetails] =
    Form(if (includeCompanyName) companyNameIncMap else defaultMap)

  private def isValid(string: String): Boolean =
    Try(util.isPossibleNumber(util.parse(string, "GB")))
      .getOrElse(false)
}

object BusinessContactDetailsFormProvider {

  private val nameRequiredError = "businessContactDetails.fullName.error.required"
  private val nameFormatError   = "businessContactDetails.simpleChars.error.format"

  private val emailRequiredError = "businessContactDetails.email.error.required"
  private val emailFormatError   = "businessContactDetails.email.error.format"

  private val phoneRequiredError = "businessContactDetails.telephoneNumber.error.required"
  private val phoneFormatError   = "businessContactDetails.telephoneNumber.error.format"

  private val companyNameRequiredError = "businessContactDetails.companyName.error.required"

  private val jobTitleRequiredError = "businessContactDetails.jobTitle.error.required"
  private val jobTitleFormatError   = "businessContactDetails.simpleChars.error.format"
}
