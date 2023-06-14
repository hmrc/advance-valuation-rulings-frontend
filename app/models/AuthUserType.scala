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

import scala.annotation.nowarn

import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant, CredentialRole, User}
import uk.gov.hmrc.auth.core.Admin

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.requests.IdentifierRequest

sealed abstract class AuthUserType(override val entryName: String) extends EnumEntry

object AuthUserType extends Enum[AuthUserType] with PlayJsonEnum[AuthUserType] {
  val values: IndexedSeq[AuthUserType] = findValues

  case object IndividualTrader extends AuthUserType("IndividualTrader")

  case object OrganisationAdmin extends AuthUserType("OrganisationAdmin") // org + user/admin
  case object OrganisationAssistant extends AuthUserType("OrganisationAssistant") // org + assistant

  case object Agent extends AuthUserType("Agent")
  
  @nowarn("cat=deprecation")
  def apply(
    affinityGroup: AffinityGroup,
    credentialRole: Option[CredentialRole]
  ): Option[AuthUserType] =
    (affinityGroup, credentialRole) match {
      case (AffinityGroup.Individual, _)                 => Some(IndividualTrader)
      case (AffinityGroup.Organisation, Some(Assistant)) => Some(OrganisationAssistant)
      case (AffinityGroup.Organisation, Some(Admin))     => Some(OrganisationAdmin)
      case (AffinityGroup.Organisation, Some(User))      => Some(OrganisationAdmin)
      case (AffinityGroup.Agent, _)                      => Some(Agent)
      case _                                             => None
    }

  def apply(request: IdentifierRequest[_]): Option[AuthUserType] =
    AuthUserType(request.affinityGroup, request.credentialRole)
}
