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

import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import play.api.data.validation.Constraints

import com.google.i18n.phonenumbers.PhoneNumberUtil
import forms.BusinessContactDetailsFormProvider._
import forms.mappings.Mappings
import models.BusinessContactDetails

class BusinessContactDetailsFormProvider @Inject() extends Mappings {

  private val util = PhoneNumberUtil.getInstance

  val nameMapping: (String, Mapping[String]) = "name" -> text(nameRequiredError)
    .verifying(Constraints.pattern(Validation.nameInputPattern, error = nameFormatError))
    .verifying(maxLength(Validation.nameMaxLength, nameLengthError))

  val emailMapping: (String, Mapping[String]) = "email" -> text(emailRequiredError)
    .verifying(maxLength(Validation.emailMaxLength, emailLengthError))
    .verifying(Constraints.pattern(Validation.emailPattern, error = emailFormatError))

  val phoneMapping: (String, Mapping[String]) = "phone" -> text(phoneRequiredError)
    .verifying(phoneLengthError, phone => phone.length <= Validation.phoneNumberMaxLength)
    .verifying(phoneLetterError, phone => !phone.exists(_.isLetter))
    .verifying(phoneFormatError, phone => isValid(phone))

  val companyNameMapping: (String, Mapping[String]) =
    "companyName" -> text(companyNameRequiredError)

  val defaultMap = mapping(nameMapping, emailMapping, phoneMapping)(
    (name, email, phone) => BusinessContactDetails.apply(name, email, phone, None)
  )(
    (businessContactDetails: BusinessContactDetails) =>
      Some(
        (
          businessContactDetails.name,
          businessContactDetails.email,
          businessContactDetails.phone
        )
      )
  )

  val companyNameIncMap = mapping(nameMapping, emailMapping, phoneMapping, companyNameMapping)(
    (name, email, phone, companyName) =>
      BusinessContactDetails.apply(name, email, phone, Some(companyName))
  )(
    (businessContactDetails: BusinessContactDetails) =>
      Some(
        (
          businessContactDetails.name,
          businessContactDetails.email,
          businessContactDetails.phone,
          businessContactDetails.companyName.getOrElse("")
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
  private val nameFormatError   = "businessContactDetails.fullName.error.format"
  private val nameLengthError   = "businessContactDetails.fullName.length"

  private val emailRequiredError = "businessContactDetails.email.error.required"
  private val emailFormatError   = "businessContactDetails.email.error.format"
  private val emailLengthError   = "businessContactDetails.email.length"

  private val phoneRequiredError = "businessContactDetails.telephoneNumber.error.required"
  private val phoneFormatError   = "businessContactDetails.telephoneNumber.error.format"
  private val phoneLengthError   = "businessContactDetails.telephoneNumber.error.length"
  private val phoneLetterError   = "businessContactDetails.telephoneNumber.error.letter"

  private val companyNameRequiredError = "businessContactDetails.companyName.error.required"
}
