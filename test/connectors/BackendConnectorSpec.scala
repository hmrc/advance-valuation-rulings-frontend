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

package connectors

import play.api.libs.json.Json

import base.SpecBase
import generators.ModelGenerators
import models.fileupload.ReadyCallbackBody
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class BackendConnectorSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {
  "ObjectStorePutRequest" - {
    "should be serializable to JSON from a callback" in {
      forAll {
        callbackBody: ReadyCallbackBody =>
          val request = BackendConnector.ObjectStorePutRequest(callbackBody)
          val json    = Json.toJson(request)

          json mustBe Json.obj(
            "uploadId"    -> callbackBody.reference,
            "downloadUrl" -> callbackBody.downloadUrl.toString,
            "fileName"    -> callbackBody.uploadDetails.fileName,
            "mimeType"    -> callbackBody.uploadDetails.fileMimeType,
            "size"        -> callbackBody.uploadDetails.size,
            "checksum"    -> callbackBody.uploadDetails.checksum
          )
      }
    }
  }
}
