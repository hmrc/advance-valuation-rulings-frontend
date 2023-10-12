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

package models

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ValuationMethod

object ValuationMethod extends Enumerable.Implicits {

  case object Method1 extends WithName("method1") with ValuationMethod

  case object Method2 extends WithName("method2") with ValuationMethod

  case object Method3 extends WithName("method3") with ValuationMethod

  case object Method4 extends WithName("method4") with ValuationMethod

  case object Method5 extends WithName("method5") with ValuationMethod

  case object Method6 extends WithName("method6") with ValuationMethod

  val values: Seq[ValuationMethod] = Seq(
    Method1,
    Method2,
    Method3,
    Method4,
    Method5,
    Method6
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = HtmlContent(
          Html(s"${messages(s"valuationMethod.${value.toString}")}")
        ),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  def optionsOld(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = HtmlContent(
          Html(s"<b>${messages(s"valuationMethod.${value.toString}")}</b>")
        ),
        value = Some(value.toString),
        id = Some(s"value_$index"),
        hint = Some(Hint(content = Text(messages(s"valuationMethod.${value.toString}.hint"))))
      )
  }

  implicit val enumerable: Enumerable[ValuationMethod] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
