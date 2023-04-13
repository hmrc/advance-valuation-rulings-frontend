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

class ContactDetailsSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import ContactDetailsSpec._

  "ContactDetails" should {

    "succeed for an individual" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
      } yield ua).success.get

      val result = ContactDetails(userAnswers, AffinityGroup.Individual)

      result shouldBe Valid(contactDetails)
    }

    "succeed for an organisation" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(BusinessContactDetailsPage, businessContactDetails)
      } yield ua).success.get

      val result = ContactDetails(userAnswers, AffinityGroup.Organisation)

      result shouldBe Valid(contactDetails)
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

object ContactDetailsSpec extends Generators {

  val CheckRegDetails = CheckRegisteredDetails(
    true,
    "eori",
    "name",
    "streetAndNumber",
    "city",
    "country",
    Some("postalCode"),
    Some("phoneNumber")
  )

  val randomString: String = stringsWithMaxLength(8).sample.get

  val draftId: DraftId = DraftId(1)

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
  val contactDetails            = ContactDetails(
    name = randomString,
    email = randomString,
    phone = Some(randomString)
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
}
