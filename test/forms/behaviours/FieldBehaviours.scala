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

package forms.behaviours

import play.api.data.{Form, FormError}

import forms.FormSpec
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait FieldBehaviours extends FormSpec with ScalaCheckPropertyChecks with Generators {

  def fieldThatBindsValidData(
    form: Form[_],
    fieldName: String,
    validDataGenerator: Gen[String]
  ): Unit =
    "bind valid data" in {

      forAll(validDataGenerator -> "validDataItem") {
        dataItem: String =>
          val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
          result.value.value mustBe dataItem
          result.errors mustBe empty
      }
    }

  def fieldThatDoesNotBindInvalidData(
    form: Form[_],
    fieldName: String,
    invalidDataGenerator: Gen[String],
    invalidError: FormError
  ): Unit =
    "must not bind invalid data" in {

      forAll(invalidDataGenerator) {
        dataItem: String =>
          val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
          result.errors mustEqual Seq(invalidError)
      }
    }

  def mandatoryField(form: Form[_], fieldName: String, requiredError: FormError): Unit = {

    "not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }

    "not bind blank values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(requiredError)
    }
  }

  def commodityCodeField(form: Form[_], fieldName: String, maxLengthKey: Seq[FormError]): Unit = {

    "binds numeric string with spaces" in {
      val result = form.bind(Map(fieldName -> "8528 711")).apply(fieldName)
      result.errors mustBe Seq.empty
    }

    "binds numeric string with 10 digits but with spaces" in {
      val result = form.bind(Map(fieldName -> "8528 711 00")).apply(fieldName)
      result.errors mustBe Seq.empty
    }

    "does not bind numeric string with more than 10 digits" in {
      val result = form.bind(Map(fieldName -> "8528 711 00 937645")).apply(fieldName)
      result.errors mustBe maxLengthKey
    }
  }

  def postcodeField(
    form: Form[_],
    fieldName: String,
    emptyPostcodeErrorKey: Seq[FormError],
    notValidPostcodeErrorKey: Seq[FormError],
    tooLongPostcodeErrorKey: Seq[FormError]
  ): Unit = {

    "bind when key is not present at all and country is not GB" in {
      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq()
    }

    "bind when key is present and country is not GB" in {
      val result = form.bind(Map(fieldName -> "", "country" -> "MD")).apply(fieldName)

      result.errors mustEqual Seq()
    }

    "bind blank values and country is not GB" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq()
    }

    "bind when key is valid post code and country is GB" in {
      val result = form.bind(Map(fieldName -> "AA1 2BB", "country" -> "GB")).apply(fieldName)
      result.errors mustEqual Seq()
    }

    "not bind when key is invalid post code and country is GB" in {
      val result = form.bind(Map(fieldName -> "postcode", "country" -> "GB")).apply(fieldName)
      result.errors mustEqual notValidPostcodeErrorKey
    }

    "not bind when key is empty post code and country is GB" in {
      val result = form.bind(Map(fieldName -> "", "country" -> "GB")).apply(fieldName)
      result.errors mustEqual emptyPostcodeErrorKey
    }

    "not bind when key is valid post code and country is not GB but is too long" ignore {
      val result =
        form.bind(Map(fieldName -> "12345123451234512345", "country" -> "MD")).apply(fieldName)
      result.errors mustEqual tooLongPostcodeErrorKey
    }
  }

  def eoriField(
    form: Form[_],
    fieldName: String,
    emptyEoriErrorKey: Seq[FormError],
    badLengthEoriErrorKey: Seq[FormError],
    badCountryCodeErrorKey: Seq[FormError],
    badCharactersErrorKey: Seq[FormError]
  ): Unit = {

    val validEori = "GB123456789012"

    "bind when key is valid EORI" in {
      val result = form.bind(Map(fieldName -> validEori)).apply(fieldName)
      result.errors mustEqual Seq()
    }

    "not bind when key is blank EORI" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual emptyEoriErrorKey
    }

    "not bind when key is EORI bad length" in {
      val result = form.bind(Map(fieldName -> validEori.dropRight(1))).apply(fieldName)
      result.errors mustEqual badLengthEoriErrorKey
    }

    "not bind when key is bad EORI country code" in {
      val result = form.bind(Map(fieldName -> ("XY" + validEori.substring(2)))).apply(fieldName)
      result.errors mustEqual badCountryCodeErrorKey
    }

    "not bind when key is bad EORI characters" in {
      val result = form.bind(Map(fieldName -> (validEori.dropRight(1) + "£"))).apply(fieldName)
      result.errors mustEqual badCharactersErrorKey
    }

  }

}
