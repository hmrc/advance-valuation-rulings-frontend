package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class DescribeTheLegalChallengesFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("describeTheLegalChallenges.error.required")
        .verifying(maxLength(8167, "describeTheLegalChallenges.error.length"))
    )
}
