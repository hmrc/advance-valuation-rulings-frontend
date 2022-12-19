package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.RequiredInformation
import play.api.data.FormError

class RequiredInformationFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new RequiredInformationFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "requiredInformation.error.required"

    behave like checkboxField[RequiredInformation](
      form,
      fieldName,
      validValues  = RequiredInformation.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
