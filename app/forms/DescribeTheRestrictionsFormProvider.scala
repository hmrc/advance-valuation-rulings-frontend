package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class DescribeTheRestrictionsFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("describeTheRestrictions.error.required")
        .verifying(maxLength(8167, "describeTheRestrictions.error.length"))
    )
}
