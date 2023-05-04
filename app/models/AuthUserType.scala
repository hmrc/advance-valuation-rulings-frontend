package models

import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant, CredentialRole, User}
import uk.gov.hmrc.auth.core.Admin

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

sealed abstract class AuthUserType(override val entryName: String) extends EnumEntry

object AuthUserType extends Enum[AuthUserType] with PlayJsonEnum[AuthUserType] {
  val values: IndexedSeq[AuthUserType] = findValues

  case object IndividualTrader extends AuthUserType("IndividualTrader")

  case object OrganisationAdmin extends AuthUserType("OrganisationAdmin") // org + user/admin
  case object OrganisationAssistant extends AuthUserType("OrganisationAssistant") // org + assistant

  def apply(
    affinityGroup: AffinityGroup,
    credentialRole: Option[CredentialRole]
  ): Option[AuthUserType] =
    (affinityGroup, credentialRole) match {
      case (AffinityGroup.Individual, _)                 => Some(IndividualTrader)
      case (AffinityGroup.Organisation, Some(Assistant)) => Some(OrganisationAssistant)
      case (AffinityGroup.Organisation, Some(Admin))     => Some(OrganisationAdmin)
      case (AffinityGroup.Organisation, Some(User))      => Some(OrganisationAdmin)
      case _                                             => None
    }
}
