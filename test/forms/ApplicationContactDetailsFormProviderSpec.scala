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

class ApplicationContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val nameRequiredKey  = "applicationContactDetails.fullName.error.required"
  val emailRequiredKey = "applicationContactDetails.email.error.required"
  val phoneRequiredKey = "applicationContactDetails.telephoneNumber.error.required"

  val form = new ApplicationContactDetailsFormProvider()()

  val validAddresses = Seq(
    "“email”@example.com",
    "email@[123.123.123.123]",
    "much.“more\\unusual”@example.com",
    "very.unusual.“@”.unusual.com@example.com",
    "\"very\\”.unusual@strange.example.com"
  )

  val invalidAddresses = Seq(
    "Abc..123@example.com",
    "email@111.222 .333.44444",
    "email@example.web",
    "email@example",
    "email..email@example.com",
    "email.@example.com",
    ".email @example.com"
  )

  ".nameField" - {
    val nameField     = "name"
    val lengthKey     = "applicationContactDetails.fullName.length"
    val nameMaxLength = 100

    behave like fieldThatBindsValidData(
      form,
      nameField,
      nonNumerics
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
    val emailField = "email"

    behave like mandatoryField(
      form,
      emailField,
      requiredError = FormError(emailField, emailRequiredKey)
    )

    for (address <- validAddresses)
      s"bind valid email: ${address}" in {
        val boundForm = form.bind(
          Map[String, String](
            "name"  -> "Julius",
            "email" -> address,
            "phone" -> "07123456789"
          )
        )
        boundForm.errors mustBe Seq.empty
      }

    for (address <- invalidAddresses)
      s"not bind invalid email: ${address}" in {
        val boundForm = form.bind(
          Map[String, String](
            "name"  -> "Julius",
            "email" -> address,
            "phone" -> "07123456789"
          )
        )
        boundForm.errors must not be Seq.empty
      }
  }

  ".phoneField" - {
    val phoneField     = "phone"
    val lengthKey      = "applicationContactDetails.fullName.length"
    val phoneMaxLength = 24

    behave like numericStringWithMaxLength(
      form,
      phoneField,
      maxLength = phoneMaxLength,
      lengthError = FormError(phoneField, lengthKey, Seq(phoneMaxLength))
    )

    behave like fieldThatBindsValidData(
      form,
      phoneField,
      numericStringsBetweenRange(0, phoneMaxLength)
    )

    behave like mandatoryField(
      form,
      phoneField,
      requiredError = FormError(phoneField, phoneRequiredKey)
    )
  }
}
