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

package viewmodels.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.textarea.Textarea

import viewmodels.ErrorMessageAwareness

object textarea extends TextareaFluency

trait TextareaFluency {

  object TextareaViewModel extends ErrorMessageAwareness {

    def apply(
      field: Field,
      label: Label
    )(implicit messages: Messages): Textarea =
      Textarea(
        id = field.id,
        name = field.name,
        value = field.value,
        label = label,
        errorMessage = errorMessage(field)
      )
  }

  implicit class FluentTextarea(textarea: Textarea) {

    def withId(id: String): Textarea =
      textarea copy (id = id)

    def describedBy(value: String): Textarea =
      textarea copy (describedBy = Some(value))

    def withHint(hint: Hint): Textarea =
      textarea copy (hint = Some(hint))

    def withCssClass(newClass: String): Textarea =
      textarea copy (classes = s"${textarea.classes} $newClass")

    def withAutocomplete(value: String): Textarea =
      textarea copy (autocomplete = Some(value))

    def withAttribute(attribute: (String, String)): Textarea =
      textarea copy (attributes = textarea.attributes + attribute)

    def withSpellcheck(on: Boolean = true): Textarea =
      textarea copy (spellcheck = Some(on))

    def withSize(size: String): Textarea =
      textarea.withCssClass(size)

    def withRows(rows: Int): Textarea =
      textarea.copy(rows = rows)
  }
}
