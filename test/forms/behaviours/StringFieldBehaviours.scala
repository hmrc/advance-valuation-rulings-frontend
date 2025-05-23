/*
 * Copyright 2025 HM Revenue & Customs
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

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(
    form: Form[?],
    fieldName: String,
    maxLength: Int,
    lengthError: FormError
  ): Unit =
    s"not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must contain only lengthError
      }
    }

  def numericStringWithMaxLength(
    form: Form[?],
    fieldName: String,
    maxLength: Int,
    lengthError: FormError
  ): Unit =
    s"not bind strings longer than $maxLength characters" in {

      forAll(numericStringsBetweenRange(maxLength + 1, Int.MaxValue) -> "aString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors contains lengthError
      }
    }

  def numericStringWithMinLength(
    form: Form[?],
    fieldName: String,
    minLength: Int,
    lengthError: FormError
  ): Unit =
    s"not bind strings less than $minLength characters" in {

      forAll(numericStringsBetweenRange(Int.MinValue, minLength - 1) -> "aString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors contains lengthError
      }
    }

  def onlyNumericField(
    form: Form[?],
    fieldName: String,
    lengthError: FormError
  ): Unit =
    s"not bind strings that should be numeric only" in {

      forAll(nonEmptyString -> "aString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors contains lengthError
      }
    }

  def alphaStringWithMaxLength(
    form: Form[?],
    fieldName: String,
    maxLength: Int,
    lengthError: FormError
  ): Unit =
    s"not bind alpha strings longer than $maxLength characters" in {

      forAll(alphaStringsWithMaxLength(maxLength)) { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors must not contain lengthError
      }
    }

  def fieldWithRange(
    form: Form[?],
    fieldName: String,
    minLength: Int,
    maxLength: Int
  ): Unit =
    s"not bind strings less than $minLength and more than $maxLength characters" in {

      forAll(numericStringsBetweenRange(minLength, maxLength) -> "aString") { string =>
        val result = form.bind(Map(fieldName -> string)).apply(fieldName)
        result.errors mustBe empty
      }
    }
}
