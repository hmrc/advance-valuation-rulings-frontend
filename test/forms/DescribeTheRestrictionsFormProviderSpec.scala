package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class DescribeTheRestrictionsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "describeTheRestrictions.error.required"
  val lengthKey = "describeTheRestrictions.error.length"
  val maxLength = 8167

  val form = new DescribeTheRestrictionsFormProvider()()

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
