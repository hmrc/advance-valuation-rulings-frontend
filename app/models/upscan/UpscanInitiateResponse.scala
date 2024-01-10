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

package models.upscan

import play.api.libs.json.{Json, OFormat}

final case class UpscanInitiateResponse(
  reference: String,
  uploadRequest: UpscanInitiateResponse.UploadRequest
)

object UpscanInitiateResponse {

  final case class UploadRequest(href: String, fields: Map[String, String])

  object UploadRequest {

    implicit lazy val format: OFormat[UploadRequest] = Json.format
  }

  implicit lazy val format: OFormat[UpscanInitiateResponse] = Json.format
}
