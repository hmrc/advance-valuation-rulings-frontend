package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class WillYouCompareToSimilarGoodsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "willYouCompareToSimilarGoods.error.required"
  val invalidKey = "error.boolean"

  val form = new WillYouCompareToSimilarGoodsFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
