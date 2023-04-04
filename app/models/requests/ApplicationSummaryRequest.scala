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

import java.time.Instant

import play.api.libs.json.{Format, Json, OFormat}

final case class ApplicationSummaryRequest(eoriNumber: String)

object ApplicationSummaryRequest {

  implicit lazy val format: OFormat[ApplicationSummaryRequest] = Json.format
}

final case class ApplicationSummaryResponse(summaries: Seq[ApplicationSummary])

object ApplicationSummaryResponse {

  implicit lazy val format: OFormat[ApplicationSummaryResponse] = Json.format
}

final case class ApplicationSummary(
  id: ApplicationId,
  goodsName: String,
  dateSubmitted: Instant,
  eoriNumber: String
)

object ApplicationSummary {

  implicit lazy val format: OFormat[ApplicationSummary] = Json.format
}
