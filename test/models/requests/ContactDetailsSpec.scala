/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import cats.data.Validated.*
import generators.*
import models.*
import org.mockito.Mockito.{mock, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*
import userrole.{UserRole, UserRoleProvider}
import ContactDetailsSpec.*
import play.api.libs.json.{JsSuccess, Json}

class ContactDetailsSpec extends SpecBase with ScalaCheckPropertyChecks with ApplicationRequestGenerator {

  val companyContactDetails: CompanyContactDetails = CompanyContactDetails(
    name = "name",
    email = "mail@email.com",
    phone = Some("0123456789")
  )

  "CompanyContactDetails" - {

    "must serialize and deserialize to/from JSON" in {
      val json = Json.toJson(companyContactDetails)
      json.validate[CompanyContactDetails] mustEqual JsSuccess(companyContactDetails)
    }

    "must fail to deserialize invalid JSON" in {
      val invalidJson = Json.obj("invalid" -> "data")
      invalidJson.validate[CompanyContactDetails].isError mustBe true
    }

    "must have a working equals and hashCode" in {
      companyContactDetails mustEqual companyContactDetails
      companyContactDetails.hashCode mustEqual companyContactDetails.hashCode
    }

    "must have a working toString" in {
      companyContactDetails.toString must include("CompanyContactDetails")
    }
  }

  "ContactDetails" - {

    val mockUserRoleProvider = mock(classOf[UserRoleProvider])
    val userRole             = mock(classOf[UserRole])

    val contactDetailsService = new ContactDetailsService(mockUserRoleProvider)

    "succeed for a given userRole" in {

      val ua = emptyUserAnswers

      val userAnswers = (for {
        ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
        ua <- ua.set(AccountHomePage, AuthUserType.IndividualTrader)

      } yield ua).success.get

      when(mockUserRoleProvider.getUserRole(userAnswers)).thenReturn(userRole)
      when(userRole.getContactDetailsForApplicationRequest(userAnswers))
        .thenReturn(Valid(contactDetails))

      val result = contactDetailsService(userAnswers)
      result mustBe Valid(contactDetails)
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
    phone = Some(randomString),
    companyName = None,
    jobTitle = Some(jobTitle)
  )
  val eoriDetails: TraderDetail                            = TraderDetail(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = "abc",
    countryCode = randomString,
    phoneNumber = None,
    isPrivate = Some(false)
  )
}
