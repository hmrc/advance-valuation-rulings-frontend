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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait AdaptMethod

object AdaptMethod extends Enumerable.Implicits {

  case object Method1 extends WithName("method1") with AdaptMethod
  case object Method2 extends WithName("method2") with AdaptMethod
  case object Method3 extends WithName("method3") with AdaptMethod
  case object Method4 extends WithName("method4") with AdaptMethod
  case object Method5 extends WithName("method5") with AdaptMethod
  case object NoOtherMethod extends WithName("method6") with AdaptMethod

  val values: Seq[AdaptMethod] = Seq(
    Method1,
    Method2,
    Method3,
    Method4,
    Method5,
    NoOtherMethod
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"adaptMethod.${value.toString}")),
      value = Some(value.toString),
      id = Some(s"value_$index")
    )
  }

  given enumerable: Enumerable[AdaptMethod] =
    Enumerable(values.map(v => v.toString -> v)*)
}
