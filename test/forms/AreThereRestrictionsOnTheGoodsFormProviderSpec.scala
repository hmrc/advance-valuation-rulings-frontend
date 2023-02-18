package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class AreThereRestrictionsOnTheGoodsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "areThereRestrictionsOnTheGoods.error.required"
  val invalidKey = "error.boolean"

  val form = new AreThereRestrictionsOnTheGoodsFormProvider()()

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
