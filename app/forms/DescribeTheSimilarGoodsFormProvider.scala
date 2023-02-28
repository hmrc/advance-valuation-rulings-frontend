package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class DescribeTheSimilarGoodsFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("describeTheSimilarGoods.error.required")
        .verifying(maxLength(8167, "describeTheSimilarGoods.error.length"))
    )
}
