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
import uk.gov.hmrc.auth.core.AffinityGroup

import generators._
import models._
import models.DraftId
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import org.scalacheck.Arbitrary
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ApplicationRequestSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import ApplicationRequestSpec._

  "ApplicationRequest" should {
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
            attachments = Seq.empty
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
            attachments = Seq.empty
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
          attachments = Seq.empty
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
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  CheckRegisteredDetails(
                    value = true,
                    eori = randomString,
                    consentToDisclosureOfPersonalData = randomBoolean,
                    name = randomString,
                    streetAndNumber = randomString,
                    city = randomString,
                    country = country.code,
                    postalCode = Some(randomString),
                    phoneNumber = Some(randomString)
                  )
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

        val result = ApplicationRequest(userAnswers, AffinityGroup.Individual)

        result shouldBe Valid(
          ApplicationRequest(
            draftId = draftId,
            trader = eoriDetails,
            agent = None,
            contact = contact,
            requestedMethod = MethodOne(
              Some("explainHowPartiesAreRelated"),
              Some("describeTheRestrictions"),
              None
            ),
            goodsDetails = goodsDetailsNoDetails,
            attachments = Seq.empty
          )
        )
      }

      "return invalid for an Individual when built from empty userAnswers" in {
        val result = ApplicationRequest(emptyUserAnswers, AffinityGroup.Individual)

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

    "when the user is an employee of an organisation" when {
      "return valid when built from correctly structured userAnswers" in {
        val ua = emptyUserAnswers

        val userAnswers = (for {
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  CheckRegisteredDetails(
                    value = true,
                    eori = randomString,
                    consentToDisclosureOfPersonalData = randomBoolean,
                    name = randomString,
                    streetAndNumber = randomString,
                    city = randomString,
                    country = country.code,
                    postalCode = Some(randomString),
                    phoneNumber = Some(randomString)
                  )
                )
          ua <- ua.set(
                  BusinessContactDetailsPage,
                  BusinessContactDetails(
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
          ua <- ua.set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
        } yield ua).success.get

        val result = ApplicationRequest(userAnswers, AffinityGroup.Organisation)

        result shouldBe Valid(
          ApplicationRequest(
            draftId = draftId,
            trader = eoriDetails,
            agent = None,
            contact = contact,
            requestedMethod = MethodOne(
              Some("explainHowPartiesAreRelated"),
              Some("describeTheRestrictions"),
              None
            ),
            goodsDetails = goodsDetailsNoDetails,
            attachments = Seq.empty
          )
        )
      }

      "return invalid when only answered is an employee on behalf of an org" in {
        val userAnswers = emptyUserAnswers
          .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)
          .get

        val result = ApplicationRequest(userAnswers, AffinityGroup.Organisation)

        result shouldBe Invalid(
          NonEmptyList.of(
            CheckRegisteredDetailsPage,
            BusinessContactDetailsPage,
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
          ua <- ua.set(DescriptionOfGoodsPage, randomString)
          ua <- ua.set(HasCommodityCodePage, false)
          ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
          ua <- ua.set(HasConfidentialInformationPage, false)
          ua <- ua.set(
                  CheckRegisteredDetailsPage,
                  CheckRegisteredDetails(
                    value = true,
                    eori = randomString,
                    consentToDisclosureOfPersonalData = randomBoolean,
                    name = randomString,
                    streetAndNumber = randomString,
                    city = randomString,
                    country = country.code,
                    postalCode = Some(randomString),
                    phoneNumber = Some(randomString)
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
          ua <- ua.set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfOrg)
          ua <- ua.set(
                  BusinessContactDetailsPage,
                  BusinessContactDetails(
                    name = randomString,
                    email = randomString,
                    phone = randomString
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

        val result = ApplicationRequest(userAnswers, AffinityGroup.Organisation)

        result shouldBe Valid(
          ApplicationRequest(
            draftId = draftId,
            trader = eoriDetails,
            agent = Some(agentEoriDetails),
            contact = contact,
            requestedMethod = MethodOne(
              Some("explainHowPartiesAreRelated"),
              Some("describeTheRestrictions"),
              None
            ),
            goodsDetails = goodsDetailsNoDetails,
            attachments = Seq.empty
          )
        )
      }

      "return invalid when only page answered is the agent on behalf of an org" in {
        val userAnswers = emptyUserAnswers
          .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
          .get

        val result = ApplicationRequest(userAnswers, AffinityGroup.Organisation)

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
       |"attachments": []
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
       |"attachments": []
    }""".stripMargin
}
