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

  ".nameField" - {
    val nameField = "name"

    behave like fieldThatBindsValidData(
      form,
      nameField,
      stringsExceptSpecificValues(Seq(""))
    )

    behave like mandatoryField(
      form,
      nameField,
      requiredError = FormError(nameField, nameRequiredKey)
    )
  }

  ".emailField" - {
    val emailField = "email"

    behave like fieldThatBindsValidData(
      form,
      emailField,
      stringsExceptSpecificValues(Seq(""))
    )

    behave like mandatoryField(
      form,
      emailField,
      requiredError = FormError(emailField, emailRequiredKey)
    )
  }

  ".phoneField" - {
    val phoneField = "phone"

    behave like fieldThatBindsValidData(
      form,
      phoneField,
      stringsExceptSpecificValues(Seq(""))
    )

    behave like mandatoryField(
      form,
      phoneField,
      requiredError = FormError(phoneField, phoneRequiredKey)
    )
  }
}
