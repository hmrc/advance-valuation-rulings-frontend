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

import models._
import pages._

case class EORIDetails(
  eori: String,
  businessName: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  postcode: String,
  country: String
)
object EORIDetails {
  implicit val format: OFormat[EORIDetails] = Json.format[EORIDetails]
}

sealed trait Applicant
case class IndividualApplicant(
  holder: EORIDetails,
  contact: ContactDetails
) extends Applicant

case class OrganisationApplicant(
  holder: EORIDetails,
  businessContact: CompanyContactDetails
) extends Applicant

object OrganisationApplicant {
  implicit val format: OFormat[OrganisationApplicant] = Json.format[OrganisationApplicant]
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

  def eoriHolder: Applicant => EORIDetails = (applicant: Applicant) =>
    applicant match {
      case IndividualApplicant(holder, _)   => holder
      case OrganisationApplicant(holder, _) => holder
    }

  def contactDetails: Applicant => Option[ContactDetails] = {
    case IndividualApplicant(_, contact) => Some(contact)
    case OrganisationApplicant(_, _)     => None
  }

  def businessContactDetails: Applicant => Option[CompanyContactDetails] = {
    case IndividualApplicant(_, _)         => None
    case OrganisationApplicant(_, contact) => Some(contact)
  }

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, Applicant] = {
    val eoriDetails = userAnswers.validatedF[CheckRegisteredDetails, EORIDetails](
      CheckRegisteredDetailsPage,
      (crd: CheckRegisteredDetails) =>
        EORIDetails(
          eori = crd.eori,
          businessName = crd.name,
          addressLine1 = crd.streetAndNumber,
          addressLine2 = "",
          addressLine3 = crd.city,
          postcode = crd.postalCode.getOrElse(""),
          country = crd.country
        )
    )

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

    (contactDetails, businessContactDetails) match {
      case (Valid(_), Invalid(_)) =>
        (eoriDetails, contactDetails).mapN {
          case (holder, contact) => IndividualApplicant(holder, contact)
        }
      case (Invalid(_), Valid(_)) =>
        (eoriDetails, businessContactDetails).mapN {
          case (holder, contact) =>
            OrganisationApplicant(holder, contact)
        }
      case _                      =>
        eoriDetails match {
          case Valid(_)   =>
            Invalid(NonEmptyList.of(ApplicationContactDetailsPage, BusinessContactDetailsPage))
          case Invalid(_) =>
            Invalid(
              NonEmptyList.of(
                CheckRegisteredDetailsPage,
                ApplicationContactDetailsPage,
                BusinessContactDetailsPage
              )
            )
        }
    }
  }
}

object IndividualApplicant {
  implicit val format: OFormat[IndividualApplicant] = Json.format[IndividualApplicant]
}
