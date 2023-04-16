/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms

import play.api.data.FormError

import forms.behaviours.StringFieldBehaviours
import generators.TraderDetailsGenerator
import models.{Country, EoriNumber}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

class AgentCompanyDetailsFormProviderSpec
    extends StringFieldBehaviours
    with TraderDetailsGenerator {

  val form = new AgentCompanyDetailsFormProvider()()

  ".agentEori" - {

    val fieldName   = "agentEori"
    val requiredKey = "agentCompanyDetails.error.agentEori.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      arbitrary[EoriNumber].map(_.value)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".agentCompanyName" - {

    val fieldName   = "agentCompanyName"
    val requiredKey = "agentCompanyDetails.error.agentCompanyName.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      arbitrary[String].suchThat(_.nonEmpty)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".agentStreetAndNumber" - {

    val fieldName   = "agentStreetAndNumber"
    val requiredKey = "agentCompanyDetails.error.agentStreetAndNumber.required"
    val lengthKey   = "agentCompanyDetails.error.agentStreetAndNumber.length"
    val maxLength   = 70

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".agentCity" - {

    val fieldName   = "agentCity"
    val requiredKey = "agentCompanyDetails.error.agentCity.required"
    val lengthKey   = "agentCompanyDetails.error.agentCity.length"
    val maxLength   = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".agentCountry" - {

    val fieldName   = "agentCountry"
    val requiredKey = "agentCompanyDetails.error.agentCountry.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Country.allCountries.map(_.code))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind any values other than valid country codes" in {

      val invalidAnswers =
        arbitrary[String]
          .suchThat(_.nonEmpty)
          .suchThat(x => !Country.allCountries.map(_.code).contains(x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("agentCountry" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
      }
    }
  }

  ".agentPostalCode" - {

    val fieldName          = "agentPostalCode"
    val lengthKey          = "agentCompanyDetails.error.agentPostalCode.length"
    val requirePostcodeKey = "agentCompanyDetails.error.agentPostalCode.required"
    val invalidPostcodeKey = "agentCompanyDetails.error.agentPostalCode.gb"
    val maxLength          = 9

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like postcodeField(
      form,
      fieldName,
      Seq(FormError(fieldName, requirePostcodeKey)),
      Seq(FormError(fieldName, invalidPostcodeKey)),
      Seq(FormError(fieldName, lengthKey, Seq(maxLength)))
    )
  }
}
