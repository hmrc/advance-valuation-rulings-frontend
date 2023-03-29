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
import cats.data._
import cats.data.Validated._
import cats.implicits._

import play.api.libs.json._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models._
import pages._

sealed trait Applicant
case class IndividualApplicant(
  contact: ContactDetails
) extends Applicant

case class OrganisationApplicant(
  businessContact: CompanyContactDetails,
  role: ImporterRole
) extends Applicant

object OrganisationApplicant {
  implicit val format: OFormat[OrganisationApplicant] = Json.format[OrganisationApplicant]
}

sealed abstract class ImporterRole(override val entryName: String) extends EnumEntry

object ImporterRole extends Enum[ImporterRole] with PlayJsonEnum[ImporterRole] {
  val values: IndexedSeq[ImporterRole] = findValues

  case object AgentOnBehalf extends ImporterRole("AgentOnBehalf")
  case object Employee extends ImporterRole("Employee")

  def apply(role: WhatIsYourRoleAsImporter): ImporterRole = role match {
    case WhatIsYourRoleAsImporter.EmployeeOfOrg      => Employee
    case WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg => AgentOnBehalf
  }
}

case class CompanyContactDetails(
  name: String,
  email: String,
  phone: Option[String],
  company: String
)
object CompanyContactDetails {
  implicit val format: OFormat[CompanyContactDetails] = Json.format[CompanyContactDetails]
}

case class ContactDetails(
  name: String,
  email: String,
  phone: Option[String]
)
object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

object Applicant {
  import ApplicationRequest._
  implicit val roleFormat: OFormat[Applicant] = Json.configured(jsonConfig).format[Applicant]

  def contactDetails: Applicant => Option[ContactDetails] = {
    case IndividualApplicant(contact) => Some(contact)
    case OrganisationApplicant(_, _)  => None
  }

  def businessContactDetails: Applicant => Option[CompanyContactDetails] = {
    case IndividualApplicant(_)            => None
    case OrganisationApplicant(contact, _) => Some(contact)
  }

  def apply(
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): ValidatedNel[Page, Applicant] = {
    val contactDetails         = userAnswers
      .validatedF[ApplicationContactDetails, ContactDetails](
        ApplicationContactDetailsPage,
        (cd: ApplicationContactDetails) => ContactDetails(cd.name, cd.email, Some(cd.phone))
      )
    val businessContactDetails =
      userAnswers
        .validatedF[BusinessContactDetails, CompanyContactDetails](
          BusinessContactDetailsPage,
          cd => CompanyContactDetails(cd.name, cd.email, Some(cd.phone), cd.company)
        )
    val role                   = userAnswers
      .validatedF[WhatIsYourRoleAsImporter, ImporterRole](
        WhatIsYourRoleAsImporterPage,
        ImporterRole.apply
      )

    affinityGroup match {
      case Individual   => contactDetails.map(IndividualApplicant(_))
      case Organisation =>
        (businessContactDetails, role).mapN(OrganisationApplicant(_, _))
      case Agent        => Invalid(NonEmptyList.one(WhatIsYourRoleAsImporterPage))
      case _            => Invalid(NonEmptyList.one(WhatIsYourRoleAsImporterPage))
    }
  }
}

object IndividualApplicant {
  implicit val format: OFormat[IndividualApplicant] = Json.format[IndividualApplicant]
}
