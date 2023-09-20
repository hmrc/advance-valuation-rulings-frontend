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

import play.api.libs.json.{Json, JsSuccess}

import config.FrontendAppConfig
import generators._
import models._
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import org.scalacheck.Arbitrary
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import userrole.UserRoleProvider

class ApplicationRequestSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import ApplicationRequestSpec._

  "ApplicationRequest" should {

    val mockAppConfig         = mock[FrontendAppConfig]
    when(mockAppConfig.agentOnBehalfOfTrader).thenReturn(false)
    val mockUserRoleProvider  = mock[UserRoleProvider]
    val contactDetailsService = new ContactDetailsService(mockAppConfig, mockUserRoleProvider)

    val applicationRequestService = new ApplicationRequestService(contactDetailsService)

    "be able to deserialize successful body" when {
      "when the user is an individual" in {
        val result = ApplicationRequest.format.reads(Json.parse(individualTraderJson))

        result.isSuccess shouldBe true

        result shouldBe JsSuccess(
          ApplicationRequest(
            draftId = draftId,
            trader = eoriDetails,
            agent = None,
            contact = contact,
            requestedMethod = requestedMethod,
            goodsDetails,
            attachments = Seq.empty,
            whatIsYourRoleResponse = WhatIsYourRoleResponse.EmployeeOrg
          )
        )
      }

      "when the user is an agent acting on behalf of an organisation" in {

        val result = ApplicationRequest.format.reads(Json.parse(agentJson))

        result.isSuccess shouldBe true
        result.get       shouldBe
          ApplicationRequest(
            draftId = draftId,
            trader = eoriDetails,
            agent = Some(agentEoriDetails),
            contact = contact,
            requestedMethod = requestedMethod,
            goodsDetails,
            attachments = Seq.empty,
            whatIsYourRoleResponse = WhatIsYourRoleResponse.AgentOrg
          )
      }
    }

    "should be able to write body" in {
      ApplicationRequest.format.writes(
        ApplicationRequest(
          draftId = draftId,
          trader = eoriDetails,
          agent = None,
          contact = contact,
          requestedMethod = requestedMethod,
          goodsDetails = goodsDetails,
          attachments = Seq.empty,
          whatIsYourRoleResponse = WhatIsYourRoleResponse.EmployeeOrg
        )
      ) shouldBe Json.parse(individualTraderJson)
    }

    "form an isomorphism" in {
      forAll {
        (applicationRequest: ApplicationRequest) =>
          val writesResult = ApplicationRequest.format.writes(applicationRequest)
          val readsResult  = ApplicationRequest.format.reads(writesResult)
          readsResult should be(JsSuccess(applicationRequest))
      }
    }

    "when the user is an individual" when {
      "return valid when built from correctly structured userAnswers" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.IndividualTrader)
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  true
                )
          ua <- ua.set(
                  ApplicationContactDetailsPage,
                  ApplicationContactDetails(
                    name = randomString,
                    email = randomString,
                    phone = randomString
                  )
                )
          ua <- ua.set(ValuationMethodPage, ValuationMethod.Method1)
          ua <- ua.set(IsThereASaleInvolvedPage, true)
          ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
          ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
          ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
          ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
          ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
          ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
          ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)

        } yield ua).success.get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Valid(
          ApplicationRequest(
            draftId = draftId,
            trader = traderDetails,
            agent = None,
            contact = contact,
            requestedMethod = MethodOne(
              Some("explainHowPartiesAreRelated"),
              Some("describeTheRestrictions"),
              None
            ),
            goodsDetails = goodsDetailsNoDetails,
            attachments = Seq.empty,
            whatIsYourRoleResponse = WhatIsYourRoleResponse.EmployeeOrg
          )
        )
      }

      "return invalid when user states registered details are incorrect" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.IndividualTrader)
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(CheckRegisteredDetailsPage, false)
          ua <- ua.set(
                  ApplicationContactDetailsPage,
                  ApplicationContactDetails(
                    name = randomString,
                    email = randomString,
                    phone = randomString
                  )
                )
          ua <- ua.set(ValuationMethodPage, ValuationMethod.Method1)
          ua <- ua.set(IsThereASaleInvolvedPage, true)
          ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
          ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
          ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
          ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
          ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
          ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
        } yield ua).success.get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage
          )
        )
      }

      "return invalid for an Individual when built from empty userAnswers" in {

        val result = applicationRequestService(
          emptyUserAnswers.set(AccountHomePage, AuthUserType.IndividualTrader).success.get,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage,
            ApplicationContactDetailsPage,
            ValuationMethodPage,
            DescriptionOfGoodsPage,
            DoYouWantToUploadDocumentsPage
          )
        )
      }
    }

    "when the user is an admin of an organisation" when {
      "return valid when built from correctly structured userAnswers" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.OrganisationAdmin)
          ua <- ua.set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  true
                )
          ua <- ua.set(
                  ApplicationContactDetailsPage,
                  ApplicationContactDetails(
                    name = randomString,
                    email = randomString,
                    phone = randomString
                  )
                )
          ua <- ua.set(ValuationMethodPage, ValuationMethod.Method1)
          ua <- ua.set(IsThereASaleInvolvedPage, true)
          ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
          ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
          ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
          ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
          ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
          ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
        } yield ua).success.get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Valid(
          ApplicationRequest(
            draftId = draftId,
            trader = traderDetails,
            agent = None,
            contact = contact,
            requestedMethod = MethodOne(
              Some("explainHowPartiesAreRelated"),
              Some("describeTheRestrictions"),
              None
            ),
            goodsDetails = goodsDetailsNoDetails,
            attachments = Seq.empty,
            whatIsYourRoleResponse = WhatIsYourRoleResponse.EmployeeOrg
          )
        )
      }

      "return invalid when only answered is an employee on behalf of an org" in {
        val userAnswers = (for {
          ua <- emptyUserAnswers.set(AccountHomePage, AuthUserType.OrganisationAdmin)
          ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)
        } yield ua).get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage,
            ApplicationContactDetailsPage,
            ValuationMethodPage,
            DescriptionOfGoodsPage,
            DoYouWantToUploadDocumentsPage
          )
        )
      }

      "return invalid when missing AccountHomePage in userAnswers" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
        } yield ua).success.get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage,
            AccountHomePage,
            ValuationMethodPage,
            DescriptionOfGoodsPage,
            DoYouWantToUploadDocumentsPage
          )
        )
      }
    }

    "when the user is an agent acting on behalf of an organisation" when {
      "return valid when built from correctly structured userAnswers" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.OrganisationAssistant)
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  true
                )
          ua <- ua.set(ValuationMethodPage, ValuationMethod.Method1)
          ua <- ua.set(IsThereASaleInvolvedPage, true)
          ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
          ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
          ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
          ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
          ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
          ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
          ua <- ua.set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfOrg)
          ua <- ua.set(
                  BusinessContactDetailsPage,
                  BusinessContactDetails(
                    name = randomString,
                    email = randomString,
                    phone = randomString,
                    companyName = None
                  )
                )
          ua <- ua.set(
                  AgentCompanyDetailsPage,
                  AgentCompanyDetails(
                    agentEoriDetails.eori,
                    agentEoriDetails.businessName,
                    agentEoriDetails.addressLine1,
                    agentEoriDetails.addressLine2.getOrElse(""),
                    country,
                    Some(agentEoriDetails.postcode)
                  )
                )
        } yield ua).success.get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Valid(
          ApplicationRequest(
            draftId = draftId,
            trader = traderDetails,
            agent = Some(agentEoriDetails),
            contact = contact,
            requestedMethod = MethodOne(
              Some("explainHowPartiesAreRelated"),
              Some("describeTheRestrictions"),
              None
            ),
            goodsDetails = goodsDetailsNoDetails,
            attachments = Seq.empty,
            whatIsYourRoleResponse = WhatIsYourRoleResponse.AgentOrg
          )
        )
      }

      "return invalid when missing is the sale subject to conditions" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.OrganisationAssistant)
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  true
                )
          ua <- ua.set(ValuationMethodPage, ValuationMethod.Method1)
          ua <- ua.set(IsThereASaleInvolvedPage, true)
          ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
          ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
          ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
          ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
          ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
          ua <- ua.set(
                  BusinessContactDetailsPage,
                  BusinessContactDetails(
                    name = randomString,
                    email = randomString,
                    phone = randomString,
                    companyName = None
                  )
                )
          ua <- ua.set(
                  AgentCompanyDetailsPage,
                  AgentCompanyDetails(
                    agentEoriDetails.eori,
                    agentEoriDetails.businessName,
                    agentEoriDetails.addressLine1,
                    agentEoriDetails.addressLine2.getOrElse(""),
                    country,
                    Some(agentEoriDetails.postcode)
                  )
                )
        } yield ua).success.get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.one(IsTheSaleSubjectToConditionsPage)
        )
      }

      "return invalid when only page answered is the agent on behalf of an org" in {
        val userAnswers = (for {
          ua <- emptyUserAnswers.set(AccountHomePage, AuthUserType.OrganisationAssistant)
          ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
        } yield ua).get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage,
            AgentCompanyDetailsPage,
            BusinessContactDetailsPage,
            ValuationMethodPage,
            DescriptionOfGoodsPage,
            DoYouWantToUploadDocumentsPage
          )
        )
      }

      "return invalid when missing AccountHomePage" in {
        val userAnswers = (for {
          ua <- emptyUserAnswers.set(AccountHomePage, AuthUserType.OrganisationAssistant)
          ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
        } yield ua).get

        val result = applicationRequestService(
          userAnswers,
          traderDetailsWithCountryCode
        )

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage,
            AgentCompanyDetailsPage,
            BusinessContactDetailsPage,
            ValuationMethodPage,
            DescriptionOfGoodsPage,
            DoYouWantToUploadDocumentsPage
          )
        )
      }
    }
  }
}

object ApplicationRequestSpec extends Generators {
  // TODO: Replace this global reused string. Reusing in multiple fields doesn't allow us to check against regressions
  //       where the data from the json are mapped to the wrong fields in the models.
  val randomString: String = stringsWithMaxLength(8).sample.get

  val randomBoolean: Boolean = Arbitrary.arbitrary[Boolean].sample.getOrElse(true)

  val draftId: DraftId = DraftId(1)

  val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

  val country = Country("GB", "United Kingdom")

  val contactInformation = ContactInformation(
    personOfContact = Some("Test Person"),
    sepCorrAddrIndicator = Some(false),
    streetAndNumber = Some("Test Street 1"),
    city = Some("Test City"),
    postalCode = Some("Test Postal Code"),
    countryCode = Some("GB"),
    telephoneNumber = Some("Test Telephone Number"),
    faxNumber = Some("Test Fax Number"),
    emailAddress = Some("Test Email Address"),
    emailVerificationTimestamp = Some("2000-01-31T23:59:59Z")
  )

  val traderDetailsWithCountryCode = TraderDetailsWithCountryCode(
    EORINo = "GB123456789012345",
    consentToDisclosureOfPersonalData = true,
    CDSFullName = "Test Name",
    CDSEstablishmentAddress = CDSEstablishmentAddress(
      streetAndNumber = "Test Street 1",
      city = "Test City",
      countryCode = "GB",
      postalCode = Some("Test Postal Code")
    ),
    contactInformation = Some(contactInformation)
  )

  val traderDetails = TraderDetail(
    eori = traderDetailsWithCountryCode.EORINo,
    businessName = traderDetailsWithCountryCode.CDSFullName,
    addressLine1 = traderDetailsWithCountryCode.CDSEstablishmentAddress.streetAndNumber,
    addressLine2 = Some(traderDetailsWithCountryCode.CDSEstablishmentAddress.city),
    addressLine3 = None,
    postcode = traderDetailsWithCountryCode.CDSEstablishmentAddress.postalCode.getOrElse(""),
    countryCode = traderDetailsWithCountryCode.CDSEstablishmentAddress.countryCode,
    phoneNumber = traderDetailsWithCountryCode.contactInformation.flatMap(_.telephoneNumber)
  )

  val eoriDetails = TraderDetail(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = randomString,
    countryCode = country.code,
    phoneNumber = Some(randomString)
  )

  val agentEoriDetails = TraderDetail(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = randomString,
    countryCode = country.code,
    phoneNumber = None
  )

  val contact = ContactDetails(
    name = randomString,
    email = randomString,
    phone = Some(randomString)
  )

  val requestedMethod = MethodThree(
    whyNotOtherMethods = randomString,
    previousSimilarGoods = PreviousSimilarGoods(randomString)
  )

  val goodsDetails = GoodsDetails(
    goodsName = randomString,
    goodsDescription = randomString,
    envisagedCommodityCode = Some(randomString),
    knownLegalProceedings = Some(randomString),
    confidentialInformation = Some(randomString)
  )

  val goodsDetailsNoDetails = GoodsDetails(
    goodsName = randomString,
    goodsDescription = randomString,
    envisagedCommodityCode = None,
    knownLegalProceedings = None,
    confidentialInformation = None
  )

  val individualTraderJson =
    s"""{
       |"draftId": "$draftId",
       |"trader": {
       |  "eori": "$randomString",
       |  "businessName": "$randomString",
       |  "addressLine1": "$randomString",
       |  "addressLine2": "$randomString",
       |  "postcode": "$randomString",
       |  "countryCode": "${country.code}",
       |  "phoneNumber": "$randomString"
       |},
       |"contact": {
       |  "name": "$randomString",
       |  "email": "$randomString",
       |  "phone": "$randomString"
       |},
       |"requestedMethod" : {
       |  "whyNotOtherMethods" : "$randomString",
       |  "previousSimilarGoods" : "$randomString",
       |  "type" : "MethodThree"
       |},
       |"goodsDetails": {
       |  "goodsName": "$randomString",
       |  "goodsDescription": "$randomString",
       |  "envisagedCommodityCode": "$randomString",
       |  "knownLegalProceedings": "$randomString",
       |  "confidentialInformation": "$randomString"
       |},
       |"attachments": [],
       |"whatIsYourRoleResponse" : "${WhatIsYourRoleResponse.EmployeeOrg}"
    }""".stripMargin

  val agentJson =
    s"""{
       |"draftId": "$draftId",
       |"trader": {
       |  "eori": "$randomString",
       |  "businessName": "$randomString",
       |  "addressLine1": "$randomString",
       |  "addressLine2": "$randomString",
       |  "postcode": "$randomString",
       |  "countryCode": "${country.code}",
       |  "phoneNumber": "$randomString"
       |},
       |"agent": {
       |  "eori": "$randomString",
       |  "businessName": "$randomString",
       |  "addressLine1": "$randomString",
       |  "addressLine2": "$randomString",
       |  "postcode": "$randomString",
       |  "countryCode": "${country.code}"
       |},
       |"contact": {
       |  "name": "$randomString",
       |  "email": "$randomString",
       |  "phone": "$randomString"
       |},
       |"requestedMethod" : {
       |  "whyNotOtherMethods" : "$randomString",
       |  "previousSimilarGoods" : "$randomString",
       |  "type" : "MethodThree"
       |},
       |"goodsDetails": {
       |  "goodsName": "$randomString",
       |  "goodsDescription": "$randomString",
       |  "envisagedCommodityCode": "$randomString",
       |  "knownLegalProceedings": "$randomString",
       |  "confidentialInformation": "$randomString"
       |},
       |"attachments": [],
       |"whatIsYourRoleResponse" : "${WhatIsYourRoleResponse.AgentOrg}"
    }""".stripMargin
}
