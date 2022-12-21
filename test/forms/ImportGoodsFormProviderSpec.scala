package forms

import play.api.data.FormError

import forms.behaviours.BooleanFieldBehaviours

class ImportGoodsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "importGoods.error.required"
  val invalidKey  = "error.boolean"

  val form = new ImportGoodsFormProvider()()

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
