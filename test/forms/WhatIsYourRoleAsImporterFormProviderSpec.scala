package forms

import forms.behaviours.OptionFieldBehaviours
import models.WhatIsYourRoleAsImporter
import play.api.data.FormError

class WhatIsYourRoleAsImporterFormProviderSpec extends OptionFieldBehaviours {

  val form = new WhatIsYourRoleAsImporterFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "whatIsYourRoleAsImporter.error.required"

    behave like optionsField[WhatIsYourRoleAsImporter](
      form,
      fieldName,
      validValues  = WhatIsYourRoleAsImporter.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
