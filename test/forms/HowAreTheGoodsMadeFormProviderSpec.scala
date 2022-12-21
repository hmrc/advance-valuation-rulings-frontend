package forms

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours

class HowAreTheGoodsMadeFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "howAreTheGoodsMade.error.required"
  val lengthKey   = "howAreTheGoodsMade.error.length"
  val maxLength   = 1000

  val form = new HowAreTheGoodsMadeFormProvider()()

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
