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

package forms.mappings

import java.time.LocalDate

import play.api.data.validation.{Invalid, Valid}

import generators.Generators
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ConstraintsSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with Generators
    with Constraints {

  "firstError" - {

    "must return Valid when all constraints pass" in {
      val result =
        firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result mustEqual Valid
    }

    "must return Invalid when the first constraint fails" in {
      val result =
        firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "must return Invalid when the second constraint fails" in {
      val result =
        firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "must return Invalid for the first error when both constraints fail" in {
      val result =
        firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" - {

    "must return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" - {

    "must return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "must return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "must return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }

  "regexp" - {

    "must return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "must return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" - {

    "must return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "must return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "must return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "must return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }

  "maxDate" - {

    "must return Valid for a date before or equal to the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), max)
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>
          val result = maxDate(max, "error.future")(date)
          result mustEqual Valid
      }
    }

    "must return Invalid for a date after the maximum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        max  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(max.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield (max, date)

      forAll(gen) {
        case (max, date) =>
          val result = maxDate(max, "error.future", "foo")(date)
          result mustEqual Invalid("error.future", "foo")
      }
    }
  }

  "minDate" - {

    "must return Valid for a date after or equal to the minimum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        date <- datesBetween(min, LocalDate.of(3000, 1, 1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>
          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Valid
      }
    }

    "must return Invalid for a date before the minimum" in {

      val gen: Gen[(LocalDate, LocalDate)] = for {
        min  <- datesBetween(LocalDate.of(2000, 1, 2), LocalDate.of(3000, 1, 1))
        date <- datesBetween(LocalDate.of(2000, 1, 1), min.minusDays(1))
      } yield (min, date)

      forAll(gen) {
        case (min, date) =>
          val result = minDate(min, "error.past", "foo")(date)
          result mustEqual Invalid("error.past", "foo")
      }
    }
  }

  "length" - {
    val threeElementSet = Set(1, 2, 3)

    "must return Valid for a set with length equal threshold" in {
      val result = length(3, "error.max").apply(threeElementSet)
      result mustEqual Valid
    }

    "must return Invalid for a set with more elements than the threshold" in {
      val result = length(2, "error.length").apply(threeElementSet)
      result mustEqual Invalid("error.length")
    }

    "must return Invalid for a set with fewer elements than the threshold" in {
      val result = length(5, "error.length").apply(threeElementSet)
      result mustEqual Invalid("error.length")
    }
  }

  "setEquals" - {
    val threeElementSet = Set("hello", "world", "!")

    "must return Valid for a set equal to the expected set" in {
      val result = setEquals(Set("hello", "!", "world"), "error.mismatch").apply(threeElementSet)
      result mustEqual Valid
    }

    "must return Invalid for a set without all the expected elements" in {
      val result = setEquals(Set("hello", "world"), "error.mismatch").apply(threeElementSet)
      result mustEqual Invalid("error.mismatch")
    }

    "must return Invalid for a set with additional elements then expected" in {
      val result =
        setEquals(Set("hello", "!", "world", "?"), "error.mismatch").apply(threeElementSet)
      result mustEqual Invalid("error.mismatch")
    }
  }
}
