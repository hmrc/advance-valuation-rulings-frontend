package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ConfidentialInformationFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "confidentialInformation.error.required"
  val lengthKey = "confidentialInformation.error.length"
  val maxLength = 1000

  val form = new ConfidentialInformationFormProvider()()

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
