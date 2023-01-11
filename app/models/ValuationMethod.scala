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
import play.api.libs.json.JsError
import play.api.libs.json.JsString
import play.api.libs.json.JsSuccess
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed abstract class ValuationMethod(val name: String)

object ValuationMethod {

  case object Method1 extends ValuationMethod("method1")
  case object Method2 extends ValuationMethod("method2")

  implicit def reads: Reads[ValuationMethod] =
    Reads {
      case JsString(str) =>
        str match {
          case "method1" => JsSuccess(Method1)
          case "method2" => JsSuccess(Method2)
          case _         => JsError("error.invalid")
        }
      case _             => JsError("error.invalid")
    }

  implicit def writes: Writes[ValuationMethod] =
    Writes(value => JsString(value.name))

  val values: Seq[ValuationMethod] = Seq(
    Method1,
    Method2
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"valuationMethod.${value.name}")),
        value = Some(value.name),
        id = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[ValuationMethod] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
