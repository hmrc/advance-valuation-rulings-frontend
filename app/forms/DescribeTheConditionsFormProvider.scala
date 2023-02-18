package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class DescribeTheConditionsFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("describeTheConditions.error.required")
        .verifying(maxLength(8167, "describeTheConditions.error.length"))
    )
}
