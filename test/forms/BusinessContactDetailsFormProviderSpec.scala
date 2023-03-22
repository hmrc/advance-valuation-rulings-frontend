package forms

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours

class BusinessContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "businessContactDetails.error.required"
  val lengthKey   = "businessContactDetails.error.length"
  val maxLength   = 8186

  val form = new BusinessContactDetailsFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
