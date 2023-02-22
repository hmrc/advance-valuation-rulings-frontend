package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class DescriptionOfGoodsFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("descriptionOfGoods.error.required")
        .verifying(maxLength(100, "descriptionOfGoods.error.length"))
    )
}
