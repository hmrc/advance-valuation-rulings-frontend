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

package models.requests

import base.SpecBase
import play.api.libs.json._

class EmailRequestSpec extends SpecBase {

  private val emailRequest: EmailRequest = EmailRequest(
    to = List(Email("test@email.com")),
    templateId = "test-template-id",
    parameters = Map("testparam" -> "testparamvalue"),
    force = true,
    eventUrl = Some("test-event-url"),
    onSendUrl = Some("test-send-url"),
    auditData = Map("tags" -> "tagsVal")
  )

  private val emailRequestJson: JsValue = Json.parse(
    """
      |{
      |    "to": [
      |        "test@email.com"
      |    ],
      |    "templateId": "test-template-id",
      |    "parameters": {
      |        "testparam": "testparamvalue"
      |    },
      |    "force": true,
      |    "eventUrl": "test-event-url",
      |    "onSendUrl": "test-send-url",
      |    "tags": {
      |        "tags": "tagsVal"
      |    }
      |}
    """.stripMargin
  )

  "EmailRequest" - {
    "must deserialise from json to an EmailRequest model correctly" in {

      emailRequestJson.as[EmailRequest] mustBe emailRequest
    }

    "must deserialise from JSON with empty parameters to an EmailRequest model correctly" in {

      val json = emailRequestJson.as[JsObject] ++ Json.obj(
        "parameters" -> Json.obj()
      )

      json.as[EmailRequest] mustBe emailRequest.copy(parameters = Map.empty)
    }

    "must deserialise from JSON with empty tags to an EmailRequest model correctly" in {

      val json = emailRequestJson.as[JsObject] ++ Json.obj(
        "tags" -> Json.obj()
      )

      json.as[EmailRequest] mustBe emailRequest.copy(auditData = Map.empty)
    }

    "must deserialise from JSON with missing parameters to an EmailRequest model correctly" in {

      val json = emailRequestJson.as[JsObject] - "parameters"

      json.as[EmailRequest] mustBe emailRequest.copy(parameters = Map.empty)
    }

    "must deserialise from JSON with missing tags to an EmailRequest model correctly" in {

      val json = emailRequestJson.as[JsObject] - "tags"

      json.as[EmailRequest] mustBe emailRequest.copy(auditData = Map.empty)
    }

    "must return JsError when the 'to' list is empty" in {

      val json = emailRequestJson.as[JsObject] ++ Json.obj(
        "to" -> Json.arr()
      )

      json.validate[EmailRequest] mustBe JsError(__ \ "to", "recipients list is empty")
    }

    "serialise EmailRequest to JSON correctly" in {

      val json = emailRequestJson.as[JsObject] - "tags" ++ Json.obj(
        "auditData" -> Map("tags" -> "tagsVal")
      )

      Json.toJson(emailRequest) mustBe json
    }
  }
}
