package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class ExplainYourGoodsComparingToSimilarGoodsFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("explainYourGoodsComparingToSimilarGoods.error.required")
        .verifying(maxLength(8167, "explainYourGoodsComparingToSimilarGoods.error.length"))
    )
}
