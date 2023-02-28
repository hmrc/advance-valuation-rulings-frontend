package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.AdaptMethod

class AdaptMethodFormProvider @Inject() extends Mappings {

  def apply(): Form[AdaptMethod] =
    Form(
      "value" -> enumerable[AdaptMethod]("adaptMethod.error.required")
    )
}
