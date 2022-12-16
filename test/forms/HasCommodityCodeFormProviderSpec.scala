package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class HasCommodityCodeFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "hasCommodityCode.error.required"
  val invalidKey = "error.boolean"

  val form = new HasCommodityCodeFormProvider()()

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
