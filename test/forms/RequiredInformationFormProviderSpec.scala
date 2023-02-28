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

import forms.behaviours.CheckboxFieldBehaviours
import models.RequiredInformation

class RequiredInformationFormProviderSpec extends CheckboxFieldBehaviours {
  import RequiredInformationFormProviderSpec._

  val form = new RequiredInformationFormProvider()()

  ".value" - {

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    "successfully bind when all 8 answers are selected" in {
      val data = allValues
      form.bind(data).errors mustBe empty
    }

    "fail to bind when less than 8 answers are selected" in {
      val data = allValues - s"$fieldName[5]"
      form.bind(data).errors must contain(
        FormError(s"$fieldName", "requiredInformation.error.selectAll")
      )
    }

    "fail to bind when invalid data is provided" in {
      val data = allValues + (s"$fieldName[8]" -> "banana")
      form.bind(data).errors must contain(
        FormError(s"$fieldName[8]", "error.invalid")
      )
    }
  }
}

object RequiredInformationFormProviderSpec {
  val fieldName   = "value"
  val requiredKey = "requiredInformation.error.selectAll"

  val allValues = Map(
    s"$fieldName[0]" -> RequiredInformation.values.head.toString,
    s"$fieldName[1]" -> RequiredInformation.values(1).toString,
    s"$fieldName[2]" -> RequiredInformation.values(2).toString,
    s"$fieldName[3]" -> RequiredInformation.values(3).toString,
    s"$fieldName[4]" -> RequiredInformation.values(4).toString,
    s"$fieldName[5]" -> RequiredInformation.values(5).toString
  )
}
