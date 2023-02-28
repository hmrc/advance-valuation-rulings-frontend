package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class DescribeTheSimilarGoodsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "describeTheSimilarGoods.error.required"
  val lengthKey = "describeTheSimilarGoods.error.length"
  val maxLength = 8167

  val form = new DescribeTheSimilarGoodsFormProvider()()

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
