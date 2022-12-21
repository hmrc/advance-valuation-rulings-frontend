package forms

import play.api.data.FormError

import forms.behaviours.OptionFieldBehaviours
import models.ValuationMethod

class ValuationMethodFormProviderSpec extends OptionFieldBehaviours {

  val form = new ValuationMethodFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "valuationMethod.error.required"

    behave like optionsField[ValuationMethod](
      form,
      fieldName,
      validValues = ValuationMethod.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
