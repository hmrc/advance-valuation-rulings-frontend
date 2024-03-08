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
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.charactercount.CharacterCount

import viewmodels.ErrorMessageAwareness

object characterCount extends CharacterCountFluency

trait CharacterCountFluency {

  object CharacterCountViewModel extends ErrorMessageAwareness {

    def apply(
      field: Field,
      label: Label
    )(implicit messages: Messages): CharacterCount =
      CharacterCount(
        id = field.id,
        name = field.name,
        value = field.value,
        label = label,
        errorMessage = errorMessage(field)
      )
  }

  implicit class FluentCharacterCount(characterCount: CharacterCount) {

    def withMaxLength(maxLength: Int): CharacterCount =
      characterCount.copy(maxLength = Some(maxLength))

    def withId(id: String): CharacterCount =
      characterCount.copy(id = id)

    def withHint(hint: Hint): CharacterCount =
      characterCount.copy(hint = Some(hint))

    def withCssClass(newClass: String): CharacterCount =
      characterCount.copy(classes = s"${characterCount.classes} $newClass")

    def withAttribute(attribute: (String, String)): CharacterCount =
      characterCount.copy(attributes = characterCount.attributes + attribute)

    def withSpellcheck(on: Boolean = true): CharacterCount =
      characterCount.copy(spellcheck = Some(on))

    def withSize(size: String): CharacterCount =
      characterCount.withCssClass(size)

    def withRows(rows: Int): CharacterCount =
      characterCount.copy(rows = rows)

    def withThreshold(threshold: Int): CharacterCount =
      characterCount.copy(threshold = Some(threshold))
  }
}
