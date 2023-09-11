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

package models.events

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialRole}

import models.WhatIsYourRoleAsImporter

final case class RoleIndicatorEvent(
  internalId: String,
  eori: String,
  affinityGroup: AffinityGroup,
  credentialRole: Option[CredentialRole],
  isAgent: Option[Boolean],
  role: Option[WhatIsYourRoleAsImporter]
)

object RoleIndicatorEvent {
  implicit val format: OFormat[RoleIndicatorEvent] = Json.format[RoleIndicatorEvent]
}
