package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ExplainHowYouWillUseMethodSixFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("explainHowYouWillUseMethodSix.error.required")
        .verifying(maxLength(8167, "explainHowYouWillUseMethodSix.error.length"))
    )
}
