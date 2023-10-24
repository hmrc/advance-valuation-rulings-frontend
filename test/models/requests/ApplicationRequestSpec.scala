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

import play.api.libs.json.{Json, JsResult, JsSuccess}

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
        val result: JsResult[ApplicationRequest] =
          ApplicationRequest.format.reads(Json.parse(individualTraderJson))

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
            whatIsYourRole = WhatIsYourRole.EmployeeOrg,
            letterOfAuthority = None
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
            whatIsYourRole = WhatIsYourRole.AgentOrg,
            letterOfAuthority = None
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
          whatIsYourRole = WhatIsYourRole.EmployeeOrg,
          letterOfAuthority = None
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
          ua <- ua.set(DescriptionOfGoodsPage, goodsDescription)
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
                    name = contactName,
                    email = contactEmail,
                    phone = contactPhone,
                    jobTitle = contactJobTitle
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
            whatIsYourRole = WhatIsYourRole.EmployeeOrg,
            letterOfAuthority = None
          )
        )
      }

      "return invalid when user states registered details are incorrect" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.IndividualTrader)
          ua <- ua.set(DescriptionOfGoodsPage, goodsDescription)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(CheckRegisteredDetailsPage, false)
          ua <- ua.set(
                  ApplicationContactDetailsPage,
                  ApplicationContactDetails(
                    name = contactName,
                    email = contactEmail,
                    phone = contactPhone,
                    jobTitle = contactJobTitle
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
          ua <- ua.set(DescriptionOfGoodsPage, goodsDescription)
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
                    name = contactName,
                    email = contactEmail,
                    phone = contactPhone,
                    jobTitle = contactJobTitle
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
            whatIsYourRole = WhatIsYourRole.EmployeeOrg,
            letterOfAuthority = None
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
          ua <- ua.set(DescriptionOfGoodsPage, goodsDescription)
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
                    name = contactName,
                    email = contactEmail,
                    phone = contactPhone,
                    companyName = None,
                    jobTitle = contactJobTitle
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
            whatIsYourRole = WhatIsYourRole.AgentOrg,
            letterOfAuthority = None
          )
        )
      }

      "return invalid when missing is the sale subject to conditions" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(AccountHomePage, AuthUserType.OrganisationAssistant)
          ua <- ua.set(DescriptionOfGoodsPage, goodsDescription)
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
                    name = contactName,
                    email = contactEmail,
                    phone = contactPhone,
                    companyName = None,
                    jobTitle = contactJobTitle
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
  val eori                  = "Trader EORI"
  val businessName          = "Trader business name"
  val addressLine1          = "Trader address line 1"
  val addressLine2          = "Trader address line 2"
  val addressLine3          = "Trader address line 3"
  val postcode              = "Trader postcode"
  val countryCode           = "Trader country code"
  val phoneNumber           = "1234567890"
  val contactName           = "contact name"
  val contactEmail          = "contact email"
  val contactPhone          = "contact phone"
  val contactJobTitle       = "contact job title"
  val goodsDescription      = "goods description"
  val goodsCommodityCode    = "envisaged commodity code"
  val goodsLegalProceedings = "known legal proceedings"
  val goodsInformation      = "confidential information"
  val requestedMethodReason = "why not other methods"
  val requestedMethodGoods  = "previous similar goods"
  val similarGoods          = "info on previous goods ruling"
  val similarMethod         = "info on previous methods ruling"

  val randomBoolean: Boolean = Arbitrary.arbitrary[Boolean].sample.getOrElse(true)

  val draftId: DraftId = DraftId(1)

  val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

  val country: Country = Country("GB", "United Kingdom")

  val contactInformation: ContactInformation = ContactInformation(
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

  val traderDetailsWithCountryCode: TraderDetailsWithCountryCode = TraderDetailsWithCountryCode(
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

  val traderDetails: TraderDetail = TraderDetail(
    eori = traderDetailsWithCountryCode.EORINo,
    businessName = traderDetailsWithCountryCode.CDSFullName,
    addressLine1 = traderDetailsWithCountryCode.CDSEstablishmentAddress.streetAndNumber,
    addressLine2 = Some(traderDetailsWithCountryCode.CDSEstablishmentAddress.city),
    addressLine3 = None,
    postcode = traderDetailsWithCountryCode.CDSEstablishmentAddress.postalCode.getOrElse(""),
    countryCode = traderDetailsWithCountryCode.CDSEstablishmentAddress.countryCode,
    phoneNumber = traderDetailsWithCountryCode.contactInformation.flatMap(_.telephoneNumber),
    isPrivate = Some(!traderDetailsWithCountryCode.consentToDisclosureOfPersonalData)
  )

  val eoriDetails: TraderDetail = TraderDetail(
    eori = eori,
    businessName = businessName,
    addressLine1 = addressLine1,
    addressLine2 = Some(addressLine2),
    addressLine3 = None,
    postcode = postcode,
    countryCode = country.code,
    phoneNumber = Some(phoneNumber),
    isPrivate = Some(randomBoolean)
  )

  val agentEoriDetails: TraderDetail = TraderDetail(
    eori = eori,
    businessName = businessName,
    addressLine1 = addressLine1,
    addressLine2 = Some(addressLine2),
    addressLine3 = None,
    postcode = postcode,
    countryCode = country.code,
    phoneNumber = None,
    isPrivate = None
  )

  val contact: ContactDetails = ContactDetails(
    name = contactName,
    email = contactEmail,
    phone = Some(contactPhone),
    companyName = None,
    jobTitle = Some(contactJobTitle)
  )

  val requestedMethod: MethodThree = MethodThree(
    whyNotOtherMethods = requestedMethodReason,
    previousSimilarGoods = PreviousSimilarGoods(requestedMethodGoods)
  )

  val goodsDetails: GoodsDetails = GoodsDetails(
    goodsName = goodsDescription,
    goodsDescription = goodsDescription,
    envisagedCommodityCode = Some(goodsCommodityCode),
    knownLegalProceedings = Some(goodsLegalProceedings),
    confidentialInformation = Some(goodsInformation),
    similarRulingGoodsInfo = Some(similarGoods),
    similarRulingMethodInfo = Some(similarMethod)
  )

  val goodsDetailsNoDetails: GoodsDetails = GoodsDetails(
    goodsName = goodsDescription,
    goodsDescription = goodsDescription,
    envisagedCommodityCode = None,
    knownLegalProceedings = None,
    confidentialInformation = None,
    similarRulingGoodsInfo = None,
    similarRulingMethodInfo = None
  )

  val individualTraderJson: String =
    s"""{
       |"draftId": "$draftId",
       |"trader": {
       |  "eori": "$eori",
       |  "businessName": "$businessName",
       |  "addressLine1": "$addressLine1",
       |  "addressLine2": "$addressLine2",
       |  "postcode": "$postcode",
       |  "countryCode": "${country.code}",
       |  "phoneNumber": "$phoneNumber",
       |  "isPrivate": $randomBoolean
       |},
       |"contact": {
       |  "name": "$contactName",
       |  "email": "$contactEmail",
       |  "phone": "$contactPhone",
       |  "jobTitle": "$contactJobTitle"
       |},
       |"requestedMethod" : {
       |  "whyNotOtherMethods" : "$requestedMethodReason",
       |  "previousSimilarGoods" : "$requestedMethodGoods",
       |  "type" : "MethodThree"
       |},
       |"goodsDetails": {
       |  "goodsName": "$goodsDescription",
       |  "goodsDescription": "$goodsDescription",
       |  "envisagedCommodityCode": "$goodsCommodityCode",
       |  "knownLegalProceedings": "$goodsLegalProceedings",
       |  "confidentialInformation": "$goodsInformation",
       |  "similarRulingGoodsInfo": "$similarGoods",
       |  "similarRulingMethodInfo": "$similarMethod"
       |},
       |"attachments": [],
       |"whatIsYourRole" : "${WhatIsYourRole.EmployeeOrg}"
    }""".stripMargin

  val agentJson: String =
    s"""{
       |"draftId": "$draftId",
       |"trader": {
       |  "eori": "$eori",
       |  "businessName": "$businessName",
       |  "addressLine1": "$addressLine1",
       |  "addressLine2": "$addressLine2",
       |  "postcode": "$postcode",
       |  "countryCode": "${country.code}",
       |  "phoneNumber": "$phoneNumber",
       |  "isPrivate": $randomBoolean
       |},
       |"agent": {
       |  "eori": "$eori",
       |  "businessName": "$businessName",
       |  "addressLine1": "$addressLine1",
       |  "addressLine2": "$addressLine2",
       |  "postcode": "$postcode",
       |  "countryCode": "${country.code}"
       |},
       |"contact": {
       |  "name": "$contactName",
       |  "email": "$contactEmail",
       |  "phone": "$contactPhone",
       |  "jobTitle": "$contactJobTitle"
       |},
       |"requestedMethod" : {
       |  "whyNotOtherMethods" : "$requestedMethodReason",
       |  "previousSimilarGoods" : "$requestedMethodGoods",
       |  "type" : "MethodThree"
       |},
       |"goodsDetails": {
       |  "goodsName": "$goodsDescription",
       |  "goodsDescription": "$goodsDescription",
       |  "envisagedCommodityCode": "$goodsCommodityCode",
       |  "knownLegalProceedings": "$goodsLegalProceedings",
       |  "confidentialInformation": "$goodsInformation",
       |  "similarRulingGoodsInfo": "$similarGoods",
       |  "similarRulingMethodInfo": "$similarMethod"
       |},
       |"attachments": [],
       |"whatIsYourRole" : "${WhatIsYourRole.AgentOrg}"
    }""".stripMargin
}
