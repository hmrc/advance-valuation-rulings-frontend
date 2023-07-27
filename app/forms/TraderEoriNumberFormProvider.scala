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

import javax.inject.Inject

import scala.util.matching.Regex

import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import forms.mappings.Mappings

class TraderEoriNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("provideTraderEori.error.required")
        .verifying(validEORINumber)
    )

  private val formatRegex: Regex = new Regex("^GB[0-9]{12}")

  private val validEORINumber: Constraint[String] =
    Constraint {
      case formatRegex()                     => Valid
      case s if s.length > 14                =>
        Invalid(ValidationError("provideTraderEori.error.tooLong"))
      case s if s.length < 14                =>
        Invalid(ValidationError("provideTraderEori.error.tooShort"))
      case s if !s.startsWith("GB")          =>
        Invalid(ValidationError("provideTraderEori.error.notGB"))
      case s if !s.forall(_.isLetterOrDigit) =>
        Invalid(ValidationError("provideTraderEori.error.specialCharacters"))
      case _                                 =>
        Invalid(ValidationError("provideTraderEori.error.default"))
    }
}
