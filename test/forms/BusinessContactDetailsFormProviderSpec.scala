package forms

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours

class BusinessContactDetailsFormProviderSpec extends StringFieldBehaviours {

  val nameRequiredKey  = "businessContactDetails.fullName.error.required"
  val emailRequiredKey = "businessContactDetails.email.error.required"
  val phoneRequiredKey = "businessContactDetails.telephoneNumber.error.required"
  val companyRequiredKey = "businessContactDetails.companyName.error.required"

  val form = new BusinessContactDetailsFormProvider()()

  val validAddresses = Seq(
    "“email”@example.com",
    "email@example.co.uk",
    "email@[123.123.123.123]",
    "much.“more\\unusual”@example.com",
    "very.unusual.“@”.unusual.com@example.com",
    "\"very\\”.unusual@strange.example.com"
  )

  val invalidAddresses = Seq(
    "Abc..123@example.com",
    "Abc..123",
    "email@111.222 .333.44444",
    "email@example",
    "email..email@example.com",
    ".email @example.com"
  )

  ".nameField" - {
    val nameField     = "name"
    val lengthKey     = "applicationContactDetails.fullName.length"
    val nameMaxLength = 100

    behave like fieldThatBindsValidData(
      form,
      nameField,
      alphaStringsWithMaxLength(100)
    )

    behave like alphaStringWithMaxLength(
      form,
      nameField,
      nameMaxLength,
      FormError(nameField, lengthKey)
    )

    behave like mandatoryField(
      form,
      nameField,
      requiredError = FormError(nameField, nameRequiredKey)
    )

  }

  ".emailField" - {
    val emailField = "email"

    behave like mandatoryField(
      form,
      emailField,
      requiredError = FormError(emailField, emailRequiredKey)
    )

    for (address <- validAddresses)
      s"bind valid email: ${address}" in {
        val boundForm = form.bind(
          Map[String, String](
            "name"  -> "Julius",
            "email" -> address,
            "phone" -> "07123456789"
          )
        )
        boundForm.errors mustBe Seq.empty
      }

    for (address <- invalidAddresses)
      s"not bind invalid email: ${address}" in {
        val boundForm = form.bind(
          Map[String, String](
            "name"  -> "Julius",
            "email" -> address,
            "phone" -> "07123456789"
          )
        )
        boundForm.errors must not be Seq.empty
      }
  }

  ".phoneField" - {
    val phoneField     = "phone"
    val lengthKey      = "applicationContactDetails.fullName.length"
    val phoneMaxLength = 24

    behave like numericStringWithMaxLength(
      form,
      phoneField,
      maxLength = phoneMaxLength,
      lengthError = FormError(phoneField, lengthKey, Seq(phoneMaxLength))
    )

    behave like fieldThatBindsValidData(
      form,
      phoneField,
      numericStringsBetweenRange(0, phoneMaxLength)
    )

    behave like mandatoryField(
      form,
      phoneField,
      requiredError = FormError(phoneField, phoneRequiredKey)
    )
  }

  ".companyNameField" - {
    val companyNameField = "companyName"

    behave like mandatoryField(
      form,
      companyNameField,
      requiredError = FormError(companyNameField, companyRequiredKey)
    )
  }
}
