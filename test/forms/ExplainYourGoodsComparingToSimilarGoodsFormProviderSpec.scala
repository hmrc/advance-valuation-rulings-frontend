package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ExplainYourGoodsComparingToSimilarGoodsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "explainYourGoodsComparingToSimilarGoods.error.required"
  val lengthKey = "explainYourGoodsComparingToSimilarGoods.error.length"
  val maxLength = 8167

  val form = new ExplainYourGoodsComparingToSimilarGoodsFormProvider()()

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
