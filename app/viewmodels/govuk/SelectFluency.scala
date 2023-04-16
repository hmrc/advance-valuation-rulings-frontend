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

package viewmodels.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.{Select, SelectItem}
import viewmodels.ErrorMessageAwareness

object select extends SelectFluency

trait SelectFluency {

  object SelectViewModel extends ErrorMessageAwareness {

    def apply(
               field: Field,
               items: Seq[SelectItem],
               label: Label
             )(implicit messages: Messages): Select =
      Select(
        id    = field.id,
        name  = field.name,
        items = items map (item => item copy (selected = field.value.isDefined && field.value == item.value)),
        label = label,
        errorMessage = errorMessage(field)
      )
  }

  implicit class FluentSelect(select: Select) {

    def withHint(hint: Hint): Select =
      select copy (hint = Some(hint))

    def describedBy(value: String): Select =
      select copy (describedBy = Some(value))

    def withFormGroupClasses(classes: String): Select =
      select copy (formGroupClasses = classes)

    def withCssClass(newClass: String): Select =
      select copy (classes = s"${select.classes} $newClass")

    def withAttribute(attribute: (String, String)): Select =
      select copy (attributes = select.attributes + attribute)
  }

  object SelectItemViewModel {

    def apply(
               value: String,
               text: String
             ): SelectItem =
      SelectItem(value = Some(value), text = text)
  }

  implicit class FluentSelectItem(item: SelectItem) {

    def disabled: SelectItem =
      item copy (disabled = true)

    def withAttribute(attribute: (String, String)): SelectItem =
      item copy (attributes = item.attributes + attribute)
  }
}
