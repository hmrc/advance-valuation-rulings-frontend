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

package models.requests

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Email(value: String)
object Email {
  implicit val format: Format[Email] = implicitly[Format[String]].inmap(Email(_), _.value)
}

case class EmailRequest(
  to: List[Email],
  templateId: String = "ars_notification_template",
  parameters: Map[String, String],
  force: Boolean = false,
  eventUrl: Option[String] = None,
  onSendUrl: Option[String] = None,
  auditData: Map[String, String] = Map.empty
)

object EmailRequest {

  implicit val format: Format[EmailRequest] = new Format[EmailRequest] {

    def reads(json: JsValue): JsResult[EmailRequest] = ((__ \ "to").read[List[Email]] and
      (__ \ "templateId").read[String] and
      ((__ \ "parameters").read[Map[String, String]] orElse Reads.pure(
        Map.empty[String, String]
      )) and
      (__ \ "force").readNullable[Boolean].map(_.getOrElse(false)) and
      (__ \ "eventUrl").readNullable[String] and
      (__ \ "onSendUrl").readNullable[String] and
      (__ \ "tags").readNullable[Map[String, String]].map(_.getOrElse(Map.empty)))(
      EmailRequest.apply _
    ).reads(json).flatMap {
      sendEmailRequest =>
        if (sendEmailRequest.to.isEmpty) {
          JsError(__ \ "to", "recipients list is empty")
        } else { JsSuccess(sendEmailRequest) }
    }
    def writes(o: EmailRequest): JsValue             = Json.writes[EmailRequest].writes(o)
  }
}
