package forms

import forms.behaviours.OptionFieldBehaviours
import models.AdaptMethod
import play.api.data.FormError

class AdaptMethodFormProviderSpec extends OptionFieldBehaviours {

  val form = new AdaptMethodFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "adaptMethod.error.required"

    behave like optionsField[AdaptMethod](
      form,
      fieldName,
      validValues  = AdaptMethod.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
