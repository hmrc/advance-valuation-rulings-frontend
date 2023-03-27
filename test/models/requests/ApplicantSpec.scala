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

import generators._
import models.{ApplicationContactDetails, ApplicationNumber, BusinessContactDetails, CheckRegisteredDetails, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
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
      val ua = UserAnswers("a", applicationNumber)

      val userAnswers = (for {
        ua <- ua.set(CheckRegisteredDetailsPage, checkRegisteredDetails)
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
      } yield ua).success.get

      val result = Applicant(userAnswers)

      result shouldBe Valid(IndividualApplicant(applicant.holder, applicant.contact))
    }

    "succeed for a business applicant" in {
      val ua = UserAnswers("a", applicationNumber)

      val userAnswers = (for {
        ua <- ua.set(CheckRegisteredDetailsPage, checkRegisteredDetails)
        ua <- ua.set(BusinessContactDetailsPage, businessContactDetails)
      } yield ua).success.get

      val result = Applicant(userAnswers)

      result shouldBe Valid(
        OrganisationApplicant(orgApplicant.holder, orgApplicant.businessContact)
      )
    }

    "return invalid when user has no contact details" in {
      val ua = UserAnswers("a", applicationNumber)

      val userAnswers = (for {
        ua <- ua.set(CheckRegisteredDetailsPage, arbitrary[CheckRegisteredDetails].sample.get)
      } yield ua).success.get

      val result = Applicant(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(ApplicationContactDetailsPage, List(BusinessContactDetailsPage))
      )
    }

    "return invalid for empty UserAnswers" in {
      val userAnswers = UserAnswers("a", applicationNumber)

      val result = Applicant(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          CheckRegisteredDetailsPage,
          List(
            ApplicationContactDetailsPage,
            BusinessContactDetailsPage
          )
        )
      )
    }
  }
}

object ApplicantSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val checkRegisteredDetails    = CheckRegisteredDetails(
    value = true,
    eori = randomString,
    name = randomString,
    streetAndNumber = randomString,
    city = randomString,
    country = randomString,
    postalCode = Some("abc")
  )
  val applicationNumber: String = ApplicationNumber("GBAVR", 1).render

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
  val applicant                 = IndividualApplicant(
    holder = EORIDetails(
      eori = randomString,
      businessName = randomString,
      addressLine1 = randomString,
      addressLine2 = "",
      addressLine3 = randomString,
      postcode = "abc",
      country = randomString
    ),
    contact = ContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString)
    )
  )
  val orgApplicant              = OrganisationApplicant(
    holder = EORIDetails(
      eori = randomString,
      businessName = randomString,
      addressLine1 = randomString,
      addressLine2 = "",
      addressLine3 = randomString,
      postcode = "abc",
      country = randomString
    ),
    businessContact = CompanyContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString),
      company = randomString
    )
  )
}
