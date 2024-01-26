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

package views

trait BaseSelectors {
  def selectElementByNumber(i: Int, element: String): String =
    if (i > 0) {
      s"$element:nth-of-type($i)"
    } else {
      s"$element:nth-of-type(1)"
    }

  def h2(i: Int = 0): String                       = selectElementByNumber(i, "main h2")
  def p(i: Int = 0): String                        = selectElementByNumber(i, "main p")
  def bullet(i: Int = 0, inGroup: Int = 1): String =
    selectElementByNumber(i, s"main ul.govuk-list.govuk-list--bullet:nth-of-type($inGroup) li")
  def dl(i: Int = 0, inGroup: Int = 1): String     =
    selectElementByNumber(i, s"main dl.govuk-summary-list:nth-of-type($inGroup) dd")

  val h1: String     = "h1"
  val h2: String     = h2()
  val p: String      = p()
  val bullet: String = bullet()

}
