/*
 * Copyright 2025 HM Revenue & Customs
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

package models.requests

import play.api.libs.json._
import play.api.mvc.PathBindable

import scala.util.Try

case class ApplicationId(value: Long) {

  override def toString: String = s"GBAVR%09d".format(value)
}

object ApplicationId {

  def apply(valueString: String): Try[ApplicationId] =
    Try(ApplicationId(valueString.toInt))

  def fromString(string: String): Option[ApplicationId] = {

    val pattern = "GBAVR(\\d{9})".r.anchored

    string match {
      case pattern(value) =>
        ApplicationId(value).toOption

      case _ =>
        None
    }
  }

  implicit def pathBindable: PathBindable[ApplicationId] =
    new PathBindable[ApplicationId] {

      override def bind(key: String, value: String): Either[String, ApplicationId] =
        fromString(value) match {
          case Some(applicationId) => Right(applicationId)
          case None                => Left("Invalid application Id")
        }

      override def unbind(key: String, value: ApplicationId): String =
        value.toString
    }

  implicit lazy val format: Format[ApplicationId] = new Format[ApplicationId] {
    override def reads(json: JsValue): JsResult[ApplicationId] =
      json match {
        case string: JsString =>
          fromString(string.value) match {
            case Some(applicationId) => JsSuccess(applicationId)
            case None                => JsError("Invalid application Id")
          }

        case _ =>
          JsError("Invalid application Id")
      }

    override def writes(o: ApplicationId): JsValue =
      JsString(o.toString)
  }
}
