package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.WhatIsYourRoleAsImporter

class WhatIsYourRoleAsImporterFormProvider @Inject() extends Mappings {

  def apply(): Form[WhatIsYourRoleAsImporter] =
    Form(
      "value" -> enumerable[WhatIsYourRoleAsImporter]("whatIsYourRoleAsImporter.error.required")
    )
}
