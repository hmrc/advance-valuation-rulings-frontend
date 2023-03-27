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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class GoodsDetailsSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import ApplicantSpec._

  "GoodsDetails" should {
    "succeed when all fields set" in {
      val ua = UserAnswers("a", applicationNumber)

      val userAnswers = (for {
        ua <- ua.set(DescriptionOfGoodsPage, randomString)
        ua <- ua.set(HasCommodityCodePage, true)
        ua <- ua.set(CommodityCodePage, randomString)
        ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, true)
        ua <- ua.set(DescribeTheLegalChallengesPage, randomString)
        ua <- ua.set(HasConfidentialInformationPage, true)
        ua <- ua.set(ConfidentialInformationPage, randomString)
      } yield ua).success.get

      val result = GoodsDetails(userAnswers)

      result shouldBe Valid(
        GoodsDetails(
          goodName = randomString,
          goodDescription = randomString,
          envisagedCommodityCode = Some(randomString),
          knownLegalProceedings = Some(randomString),
          confidentialInformation = Some(randomString)
        )
      )
    }

    "return invalid for empty UserAnswers" in {
      val userAnswers = UserAnswers("a", applicationNumber)

      val result = GoodsDetails(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(
          DescriptionOfGoodsPage
        )
      )
    }
  }
}

object GoodsDetailsSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val checkRegisteredDetails    = CheckRegisteredDetails(
    value = true,
    eori = randomString,
    name = randomString,
    streetAndNumber = randomString,
    city = randomString,
    country = randomString,
    postalCode = Some(randomString)
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
      postcode = randomString,
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
      postcode = randomString,
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
