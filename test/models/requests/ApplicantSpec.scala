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

import cats.data.NonEmptyList
import cats.data.Validated._

import uk.gov.hmrc.auth.core.AffinityGroup

import generators._
import models._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ApplicantSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import ApplicantSpec._

  "Applicant" should {
    "succeed for an individual applicant" in {
      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
      } yield ua).success.get

      val result = Applicant(userAnswers, AffinityGroup.Individual)

      result shouldBe Valid(IndividualApplicant(applicant.contact))
    }

    "succeed for a business applicant" in {
      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(BusinessContactDetailsPage, businessContactDetails)
        ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
      } yield ua).success.get

      val result = Applicant(userAnswers, AffinityGroup.Organisation)

      result shouldBe Valid(
        OrganisationApplicant(orgApplicant.businessContact, orgApplicant.role)
      )
    }

    "return invalid when Organisation has no contact details" in {
      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(CheckRegisteredDetailsPage, CheckRegDetails)
        ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)

      } yield ua).success.get

      val result = Applicant(userAnswers, AffinityGroup.Organisation)

      result shouldBe Invalid(
        NonEmptyList.of(BusinessContactDetailsPage)
      )
    }

    "return invalid when Individual has no contact details" in {
      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(CheckRegisteredDetailsPage, CheckRegDetails)
      } yield ua).success.get

      val result = Applicant(userAnswers, AffinityGroup.Individual)

      result shouldBe Invalid(
        NonEmptyList.of(ApplicationContactDetailsPage)
      )
    }

    "return invalid for an Individual with empty UserAnswers" in {
      val userAnswers = emptyUserAnswers

      val result = Applicant(userAnswers, AffinityGroup.Individual)

      result shouldBe Invalid(
        NonEmptyList.one(ApplicationContactDetailsPage)
      )
    }

    "return invalid for an Org Applicant with empty UserAnswers" in {
      val userAnswers = emptyUserAnswers

      val result = Applicant(userAnswers, AffinityGroup.Organisation)

      result shouldBe Invalid(
        NonEmptyList.of(BusinessContactDetailsPage, WhatIsYourRoleAsImporterPage)
      )
    }
  }

  "ContactDetails" should {

    "succeed for an individual" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
      } yield ua).success.get

      val result = ContactDetails(userAnswers, AffinityGroup.Individual)

      result shouldBe Valid(applicant.contact)
    }

    "succeed for an organisation" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(BusinessContactDetailsPage, businessContactDetails)
      } yield ua).success.get

      val result = ContactDetails(userAnswers, AffinityGroup.Organisation)

      result shouldBe Valid(applicant.contact)
    }

    "fail when individual contact details are missing" in {

      val result = ContactDetails(emptyUserAnswers, AffinityGroup.Individual)

      result shouldBe Invalid(NonEmptyList.one(ApplicationContactDetailsPage))
    }

    "fail when organisation contact details are missing" in {

      val result = ContactDetails(emptyUserAnswers, AffinityGroup.Organisation)

      result shouldBe Invalid(NonEmptyList.one(BusinessContactDetailsPage))
    }
  }
}

object ApplicantSpec extends Generators {

  val CheckRegDetails = CheckRegisteredDetails(
    true,
    "eori",
    "name",
    "streetAndNumber",
    "city",
    "country",
    Some("postalCode")
  )

  val randomString: String = stringsWithMaxLength(8).sample.get

  val draftId: String = DraftId("DRAFT", 1).render

  val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

  val applicationContactDetails = ApplicationContactDetails(
    name = randomString,
    email = randomString,
    phone = randomString
  )
  val businessContactDetails    = BusinessContactDetails(
    name = randomString,
    email = randomString,
    phone = randomString,
    company = randomString
  )
  val eoriDetails               = TraderDetail(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = "abc",
    countryCode = randomString,
    phoneNumber = None
  )
  val applicant                 = IndividualApplicant(
    contact = ContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString)
    )
  )
  val orgApplicant              = OrganisationApplicant(
    businessContact = CompanyContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString),
      company = randomString
    ),
    role = ImporterRole.AgentOnBehalf
  )
}
