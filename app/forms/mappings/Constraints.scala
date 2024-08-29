/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import java.time.LocalDate
import scala.util.matching.Regex

trait Constraints {

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit
    ev: Ordering[A]
  ): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input >= minimum) {
        Valid
      } else {
        Invalid(errorKey, minimum)
      }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit
    ev: Ordering[A]
  ): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _                         =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _                            =>
        Invalid(errorKey, maximum)
    }

  protected def numericAndCorrectLength(minimum: Int, maximum: Int): Constraint[String] =
    Constraint { str =>
      if (isNumeric(str)) {
        if (hasCorrectLength(str, minimum, maximum)) {
          Valid
        } else {
          Invalid("commodityCode.error.length")
        }
      } else {
        Invalid("commodityCode.error.nonNumeric")
      }
    }

  private def isNumeric(str: String): Boolean =
    str.replaceAll("\\s", "").forall(_.isDigit)

  private def hasCorrectLength(str: String, minimum: Int, maximum: Int): Boolean =
    (minimum to maximum).contains(str.length)

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _                             =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _                              =>
        Valid
    }

  protected def length[A](expected: Int, errorKey: String): Constraint[Set[_]] =
    Constraint(set => if (set.size == expected) Valid else Invalid(errorKey))

  protected def setEquals[A](expected: Set[_], errorKey: String): Constraint[Set[_]] =
    Constraint(set => if (set == expected) Valid else Invalid(errorKey))

  private val eoriCodeRegex: Regex = new Regex("^(?i)GB[0-9]{12}$")
  private val eoriExpectedLength   = 14

  protected def eoriCode(badLengthErrorMessage: String): Constraint[String] =
    Constraint("constraints.eoriFormat") { s =>
      s.replace(" ", "") match {
        case eoriCodeRegex()                      => Valid
        case s if s.length != eoriExpectedLength  =>
          Invalid(ValidationError(badLengthErrorMessage))
        case s if !s.toUpperCase.startsWith("GB") =>
          Invalid(ValidationError("provideTraderEori.error.notGB"))
        case s if !s.forall(_.isLetterOrDigit)    =>
          Invalid(ValidationError("provideTraderEori.error.specialCharacters"))
        case _                                    => Invalid(ValidationError("provideTraderEori.error.default"))
      }
    }

}
