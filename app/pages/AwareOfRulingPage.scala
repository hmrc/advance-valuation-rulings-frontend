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

package pages

import scala.util.Try

import play.api.libs.json.JsPath

import models.UserAnswers

case object AwareOfRulingPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString()

  override def toString: String = "awareOfRulingPage"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(AboutSimilarGoodsPage)
      case _           => super.cleanup(value, userAnswers)
    }
}
