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

import play.api.libs.json._

final case class CounterWrapper(_id: CounterId, index: Long)

object CounterWrapper {

  implicit lazy val format: OFormat[CounterWrapper] = Json.format
}

sealed trait CounterId

object CounterId {

  case object DraftId extends CounterId {
    override val toString: String = "draftId"
  }

  implicit lazy val format: Format[CounterId] = new Format[CounterId] {
    override def reads(json: JsValue): JsResult[CounterId] =
      json match {
        case JsString(DraftId.toString) => JsSuccess(DraftId)
        case _                          => JsError("Invalid counter id")
      }

    override def writes(o: CounterId): JsValue =
      JsString(o.toString)
  }
}
