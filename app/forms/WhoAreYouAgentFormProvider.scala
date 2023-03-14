package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class WhoAreYouAgentFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("whoAreYouAgent.error.required")
        .verifying(maxLength(100, "whoAreYouAgent.error.length"))
    )
}
