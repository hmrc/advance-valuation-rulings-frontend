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

package forms

import base.SpecBase
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CommodityCodeFormProviderSpec extends StringFieldBehaviours with SpecBase {

  val requiredKey       = "commodityCode.error.required"
  val lengthKey         = "commodityCode.error.length"
  val minLength         = 4
  val maxLength         = 10
  val invalidNumericKey = "commodityCode.error.nonNumeric"

  val form = new CommodityCodeFormProvider()()

  "CommodityCodeFormProvider" - {
    ".value" - {

      val fieldName = "value"

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        numericStringsBetweenRange(minLength, maxLength)
      )

      behave like onlyNumericField(
        form,
        fieldName,
        lengthError = FormError(fieldName, invalidNumericKey, Seq())
      )

      behave like numericStringWithMinLength(
        form,
        fieldName,
        minLength = minLength,
        lengthError = FormError(fieldName, lengthKey, Seq(minLength))
      )

      behave like numericStringWithMaxLength(
        form,
        fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
      )

      behave like fieldWithRange(
        form,
        fieldName,
        minLength = minLength,
        maxLength = maxLength
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )

      behave like commodityCodeField(
        form,
        fieldName,
        Seq(FormError(fieldName, lengthKey))
      )
    }
  }
}
