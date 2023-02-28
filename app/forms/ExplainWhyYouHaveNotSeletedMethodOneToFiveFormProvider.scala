package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ExplainWhyYouHaveNotSeletedMethodOneToFiveFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("explainWhyYouHaveNotSeletedMethodOneToFive.error.required")
        .verifying(maxLength(8167, "explainWhyYouHaveNotSeletedMethodOneToFive.error.length"))
    )
}
