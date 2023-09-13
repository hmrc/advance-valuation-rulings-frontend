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

import scala.util.matching.Regex

object Validation {

  val nameMaxLength        = 70
  val phoneNumberMaxLength = 24
  val emailMaxLength       = 50

  val nameInputPattern: Regex = "[A-Za-zÀ-ÖØ-öø-ÿĀ-ňŊ-ſ'’ -]+".r.anchored
  val emailPattern: Regex     = """^\S+@\S+$""".r

  def phoneFormat(phoneNumber: String): Boolean = {
    val number             = phoneNumber.replace("+", "")
    val containsOnlyDigits = number.forall(_.isDigit)
    val isShortEnough      = number.length <= phoneNumberMaxLength
    val isNotEmpty         = number.nonEmpty
    containsOnlyDigits && isShortEnough && isNotEmpty
  }

}
