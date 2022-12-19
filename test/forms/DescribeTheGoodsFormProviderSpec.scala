package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class DescribeTheGoodsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "describeTheGoods.error.required"
  val lengthKey = "describeTheGoods.error.length"
  val maxLength = 1000

  val form = new DescribeTheGoodsFormProvider()()

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
