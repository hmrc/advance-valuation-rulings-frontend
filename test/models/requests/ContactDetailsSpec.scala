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

import config.FrontendAppConfig
import generators._
import models._
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import userrole.UserRoleProvider

class ContactDetailsSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import ContactDetailsSpec._

  "ContactDetails" should {

    val mockAppConfig        = mock[FrontendAppConfig]
    when(mockAppConfig.agentOnBehalfOfTrader).thenReturn(false)
    val mockUserRoleProvider = mock[UserRoleProvider]

    val contactDetailsService = new ContactDetailsService(mockAppConfig, mockUserRoleProvider)

    "succeed for an individual" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
        ua <- ua.set(AccountHomePage, AuthUserType.IndividualTrader)

      } yield ua).success.get

      val result = contactDetailsService(userAnswers)
      result shouldBe Valid(contactDetails)
    }

    "succeed for an organisation admin" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
        ua <- ua.set(AccountHomePage, AuthUserType.OrganisationAdmin)

      } yield ua).success.get

      val result = contactDetailsService(userAnswers)
      result shouldBe Valid(contactDetails)
    }

    "succeed for an organisation assistant" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(BusinessContactDetailsPage, businessContactDetails)
        ua <- ua.set(AccountHomePage, AuthUserType.OrganisationAssistant)

      } yield ua).success.get

      val result = contactDetailsService(userAnswers)
      result shouldBe Valid(contactDetails)
    }

    "fail when individual contact details are missing" in {
      val userAnswers = emptyUserAnswers.set(AccountHomePage, AuthUserType.IndividualTrader).get

      val result = contactDetailsService(userAnswers)
      result shouldBe Invalid(NonEmptyList.one(ApplicationContactDetailsPage))
    }

    "fail when organisation contact details are missing" in {
      val userAnswers =
        emptyUserAnswers.set(AccountHomePage, AuthUserType.OrganisationAssistant).get

      val result = contactDetailsService(userAnswers)
      result shouldBe Invalid(NonEmptyList.one(BusinessContactDetailsPage))
    }
  }
}

object ContactDetailsSpec extends Generators {

  val randomString: String = stringsWithMaxLength(8).sample.get

  val jobTitle: String = "CEO"

  val draftId: DraftId = DraftId(1)

  val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

  val applicationContactDetails: ApplicationContactDetails = ApplicationContactDetails(
    name = randomString,
    email = randomString,
    phone = randomString,
    jobTitle = jobTitle
  )
  val businessContactDetails: BusinessContactDetails       = BusinessContactDetails(
    name = randomString,
    email = randomString,
    phone = randomString,
    companyName = None,
    jobTitle = jobTitle
  )
  val contactDetails: ContactDetails                       = ContactDetails(
    name = randomString,
    email = randomString,
    phone = Some(randomString)
  )
  val eoriDetails: TraderDetail                            = TraderDetail(
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
