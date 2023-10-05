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

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen

class BusinessContactDetailsFormProviderSpec extends StringFieldBehaviours {

  private val nameRequiredKey    = "businessContactDetails.fullName.error.required"
  private val emailRequiredKey   = "businessContactDetails.email.error.required"
  private val phoneRequiredKey   = "businessContactDetails.telephoneNumber.error.required"
  private val companyRequiredKey = "businessContactDetails.companyName.error.required"

  private val form = new BusinessContactDetailsFormProvider()(true)

  private val validAddresses = Seq(
    "“email”@example.com",
    "email@example.co.uk",
    "email@[123.123.123.123]",
    "much.“more\\unusual”@example.com",
    "very.unusual.“@”.unusual.com@example.com",
    "\"very\\”.unusual@strange.example.com"
  )

  private val invalidAddresses =
    Seq("Abc..123example.com", "Abc..123", "email@111.222 .333.44444", "453235", "@", "test@")

  ".nameField" - {
    val nameField     = "name"
    val lengthKey     = "applicationContactDetails.fullName.length"
    val invalidKey    = "businessContactDetails.simpleChars.error.format"
    val nameMaxLength = 70

    behave like fieldThatBindsValidData(
      form,
      nameField,
      safeNameInputsWithMaxLength(nameMaxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      nameField,
      unsafeInputsWithMaxLength(nameMaxLength),
      FormError(nameField, invalidKey, Seq(Validation.nameInputPattern))
    )

    behave like alphaStringWithMaxLength(
      form,
      nameField,
      nameMaxLength,
      FormError(nameField, lengthKey)
    )

    behave like mandatoryField(
      form,
      nameField,
      requiredError = FormError(nameField, nameRequiredKey)
    )

  }

  ".emailField" - {
    val emailField     = "email"
    val emailMaxLength = 50
    val lengthKey      = "businessContactDetails.email.length"

    behave like mandatoryField(
      form,
      emailField,
      requiredError = FormError(emailField, emailRequiredKey)
    )

    behave like alphaStringWithMaxLength(
      form,
      emailField,
      emailMaxLength,
      FormError(emailField, lengthKey)
    )

    for (address <- validAddresses)
      s"bind valid email: $address" in {
        val boundForm = form.bind(
          Map[String, String](
            "name"        -> "Julius",
            "email"       -> address,
            "phone"       -> "07123456789",
            "companyName" -> "company",
            "jobTitle"    -> "CEO"
          )
        )
        boundForm.errors mustBe Seq.empty
      }

    for (address <- invalidAddresses)
      s"not bind invalid email: $address" in {
        val boundForm = form.bind(
          Map[String, String](
            "name"        -> "Julius",
            "email"       -> address,
            "phone"       -> "07123456789",
            "companyName" -> "company",
            "jobTitle"    -> "CEO"
          )
        )
        boundForm.errors must not be Seq.empty
      }
  }

  ".phoneField" - {
    val phoneField     = "phone"
    val lengthKey      = "applicationContactDetails.fullName.length"
    val phoneMaxLength = 25

    behave like numericStringWithMaxLength(
      form,
      phoneField,
      maxLength = phoneMaxLength,
      lengthError = FormError(phoneField, lengthKey, Seq(phoneMaxLength))
    )

    behave like fieldThatBindsValidData(
      form,
      phoneField,
      Gen.oneOf(
        "07777777777",
        "+447777777777",
        " 07777777777  ",
        "070 0000 0000",
        "+44130000000",
        "01632 960 001",
        "07700 900 982",
        "07700-900-982",
        "+44 808 157 0192",
        "070 0000 0000",
        "+44131400834"
      )
    )

    behave like mandatoryField(
      form,
      phoneField,
      requiredError = FormError(phoneField, phoneRequiredKey)
    )

    "fail to bind an invalid phone number" in {
      val result       = form.bind(Map(phoneField -> "invalid")).apply(phoneField)
      val errorMessage = result.error.value.message
      errorMessage mustEqual "businessContactDetails.telephoneNumber.error.format"
    }

    "fail to bind an phone number with trailing letters" in {
      val result       = form.bind(Map(phoneField -> "070 0000 000z")).apply(phoneField)
      val errorMessage = result.error.value.message
      errorMessage mustEqual "businessContactDetails.telephoneNumber.error.format"
    }

    "fail to bind a phone number that is too long" in {
      val length       = Validation.phoneNumberMaxLength + 1
      val result       = form
        .bind(Map(phoneField -> "0" * length))
        .apply(phoneField)
      val errorMessage = result.error.value.message
      errorMessage mustEqual "businessContactDetails.telephoneNumber.error.format"
    }
  }

  ".companyName" - {
    val companyField = "companyName"

    behave like mandatoryField(
      form,
      companyField,
      requiredError = FormError(companyField, companyRequiredKey)
    )
  }

}
