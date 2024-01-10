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

import models.UserAnswers
import org.scalatest.TryValues._
import play.api.libs.json.Writes
import queries.Modifiable

package object pages {

  implicit class UserAnswersSetHelper(ua: UserAnswers) {
    def unsafeSet[A](page: Modifiable[A])(value: A)(implicit writes: Writes[A]): UserAnswers =
      ua.set(page, value).success.value
  }

}
