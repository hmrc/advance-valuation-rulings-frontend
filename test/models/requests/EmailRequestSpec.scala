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

import base.SpecBase
import play.api.libs.json.Json

class EmailRequestSpec extends SpecBase {

  "EmailRequest" - {
    "read json to an EmailRequest model" in {
      val emailRequestJson = Json.parse("""{
          |"to": ["test@email.com"],
          |"templateId": "test-template-id",
          |"parameters": {"testparam": "testparamvalue"},
          |"force": true,
          |"eventUrl": "test-event-url",
          |"onSendUrl": "test-send-url",
          |"tags": {"tags": "tagsVal"}
          |}
          |""".stripMargin)

      emailRequestJson.as[EmailRequest] mustBe EmailRequest(
        List(Email("test@email.com")),
        "test-template-id",
        Map("testparam" -> "testparamvalue"),
        force = true,
        Some("test-event-url"),
        Some("test-send-url"),
        Map("tags"      -> "tagsVal")
      )
    }
  }
}
