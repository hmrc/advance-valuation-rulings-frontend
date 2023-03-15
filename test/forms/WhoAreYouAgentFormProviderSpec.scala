package forms

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours

class WhoAreYouAgentFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "whoAreYouAgent.error.required"
  val lengthKey   = "whoAreYouAgent.error.length"
  val maxLength   = 100

  val form = new WhoAreYouAgentFormProvider()()

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
