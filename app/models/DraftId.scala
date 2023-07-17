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

import scala.util.Try

import play.api.libs.json._
import play.api.mvc.PathBindable

final case class DraftId(value: Long) {
  override val toString: String = s"DRAFT%09d".format(value)
}

object DraftId {

  def apply(valueString: String): Try[DraftId] =
    Try(DraftId(valueString.toInt))

  def fromString(string: String): Option[DraftId] = {

    val pattern = "DRAFT(\\d{9})".r.anchored

    string match {
      case pattern(value) =>
        DraftId(value).toOption

      case _ =>
        None
    }
  }

  implicit def pathBindable: PathBindable[DraftId] =
    new PathBindable[DraftId] {

      override def bind(key: String, value: String): Either[String, DraftId] =
        fromString(value) match {
          case Some(draftId) => Right(draftId)
          case None          => Left("Invalid draft Id")
        }

      override def unbind(key: String, value: DraftId): String =
        value.toString
    }

  implicit lazy val format: Format[DraftId] = new Format[DraftId] {
    override def reads(json: JsValue): JsResult[DraftId] =
      json match {
        case string: JsString =>
          fromString(string.value) match {
            case Some(draftId) => JsSuccess(draftId)
            case None          => JsError("Invalid draft Id")
          }

        case _ =>
          JsError("Invalid draft Id")
      }

    override def writes(o: DraftId): JsValue =
      JsString(o.toString)
  }
}
