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

case class AgentCompanyDetails(
  agentEori: String,
  agentCompanyName: String,
  agentStreetAndNumber: String,
  agentCity: String,
  agentCountry: String,
  agentPostalCode: Option[String] // TODO: Make Postcode mandatory
)

//EORI (mandatory), business name (mandatory), business address (same pattern as the others, e.g. line 1 mandatory etc.
object AgentCompanyDetails {
  implicit val format = Json.format[AgentCompanyDetails]
}
