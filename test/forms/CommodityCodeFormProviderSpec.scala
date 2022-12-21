package forms

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours

class CommodityCodeFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "commodityCode.error.required"
  val lengthKey   = "commodityCode.error.length"
  val maxLength   = 100

  val form = new CommodityCodeFormProvider()()

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
