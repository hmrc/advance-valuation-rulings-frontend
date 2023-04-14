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
import uk.gov.hmrc.auth.core.AffinityGroup

import forms.behaviours.BooleanFieldBehaviours

class CheckRegisteredDetailsFormProviderSpec extends BooleanFieldBehaviours {

  "for individuals" - {

    val form = new CheckRegisteredDetailsFormProvider()(AffinityGroup.Individual)

    ".value" - {

      val fieldName   = "value"
      val requiredKey = "checkRegisteredDetails.error.required.individual"
      val invalidKey  = "error.boolean"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }

  "for organisations" - {

    val form = new CheckRegisteredDetailsFormProvider()(AffinityGroup.Organisation)

    ".value" - {

      val fieldName   = "value"
      val requiredKey = "checkRegisteredDetails.error.required.organisation"
      val invalidKey  = "error.boolean"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }

  "for agents" - {

    val form = new CheckRegisteredDetailsFormProvider()(AffinityGroup.Agent)

    ".value" - {

      val fieldName   = "value"
      val requiredKey = "checkRegisteredDetails.error.required.agent"
      val invalidKey  = "error.boolean"

      behave like booleanField(
        form,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }
}
