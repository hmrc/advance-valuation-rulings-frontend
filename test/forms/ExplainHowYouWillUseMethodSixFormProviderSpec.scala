package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ExplainHowYouWillUseMethodSixFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "explainHowYouWillUseMethodSix.error.required"
  val lengthKey = "explainHowYouWillUseMethodSix.error.length"
  val maxLength = 8167

  val form = new ExplainHowYouWillUseMethodSixFormProvider()()

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
