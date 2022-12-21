package forms

import play.api.data.FormError

import forms.behaviours.IntFieldBehaviours

class PriceOfGoodsFormProviderSpec extends IntFieldBehaviours {

  val form = new PriceOfGoodsFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = 100000000

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError = FormError(fieldName, "priceOfGoods.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "priceOfGoods.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "priceOfGoods.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "priceOfGoods.error.required")
    )
  }
}
