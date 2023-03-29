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
import models.ApplicationNumber
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
    "be able to deserialize successful body" in {
      ApplicationRequest.format.reads(Json.parse(body)) shouldBe JsSuccess(
        ApplicationRequest(
          applicationNumber = applicationNumber,
          eoriDetails = eoriDetails,
          applicant = applicant,
          requestedMethod = requestedMethod,
          goodsDetails,
          attachments = Seq.empty
        )
      )
    }

    "should be able to write body" in {
      ApplicationRequest.format.writes(
        ApplicationRequest(
          applicationNumber = applicationNumber,
          eoriDetails = eoriDetails,
          applicant = applicant,
          requestedMethod = requestedMethod,
          goodsDetails = goodsDetails,
          attachments = Seq.empty
        )
      ) shouldBe Json.parse(body)
    }

    "form an isomorphism" in {
      forAll {
        (applicationRequest: ApplicationRequest) =>
          val writesResult = ApplicationRequest.format.writes(applicationRequest)
          val readsResult  = ApplicationRequest.format.reads(writesResult)
          readsResult should be(JsSuccess(applicationRequest))
      }
    }

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
                  name = randomString,
                  streetAndNumber = randomString,
                  city = randomString,
                  country = randomString,
                  postalCode = Some(randomString)
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
          applicationNumber = applicationNumber,
          eoriDetails = eoriDetails,
          applicant = applicant,
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

    "return invalid for an Organisation when built from empty userAnswers" in {

      val result = ApplicationRequest(emptyUserAnswers, AffinityGroup.Organisation)

      result shouldBe Invalid(
        NonEmptyList.of(
          CheckRegisteredDetailsPage,
          BusinessContactDetailsPage,
          WhatIsYourRoleAsImporterPage,
          ValuationMethodPage,
          DescriptionOfGoodsPage,
          DoYouWantToUploadDocumentsPage
        )
      )
    }
  }
}

object ApplicationRequestSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val applicationNumber: String = ApplicationNumber("GBAVR", 1).render

  val emptyUserAnswers: UserAnswers = UserAnswers("a", applicationNumber)

  val eoriDetails = EORIDetails(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = "",
    addressLine3 = randomString,
    postcode = randomString,
    country = randomString
  )

  val applicant = IndividualApplicant(
    contact = ContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString)
    )
  )

  val requestedMethod = MethodThree(
    whyNotOtherMethods = randomString,
    detailedDescription = PreviousSimilarGoods(randomString)
  )

  val goodsDetails = GoodsDetails(
    goodName = randomString,
    goodDescription = randomString,
    envisagedCommodityCode = Some(randomString),
    knownLegalProceedings = Some(randomString),
    confidentialInformation = Some(randomString)
  )

  val goodsDetailsNoDetails = GoodsDetails(
    goodName = randomString,
    goodDescription = randomString,
    envisagedCommodityCode = None,
    knownLegalProceedings = None,
    confidentialInformation = None
  )

  val body =
    s"""{
    |"applicationNumber": "$applicationNumber",
    |"eoriDetails": {
    |  "eori": "$randomString",
    |  "businessName": "$randomString",
    |  "addressLine1": "$randomString",
    |  "addressLine2": "",
    |  "addressLine3": "$randomString",
    |  "postcode": "$randomString",
    |  "country": "$randomString"
    |},
    |"applicant": {
    |  "contact": {
    |    "name": "$randomString",
    |    "email": "$randomString",
    |    "phone": "$randomString"
    |  },
    |  "_type": "IndividualApplicant"
    |},
    |"requestedMethod" : {
    |  "whyNotOtherMethods" : "$randomString",
    |  "detailedDescription" : {
    |    "_value" : "$randomString",
    |    "_type" : "PreviousSimilarGoods"
    |  },
    |  "_type" : "MethodThree"
    |},
    |"goodsDetails": {
    |  "goodName": "$randomString",
    |  "goodDescription": "$randomString",
    |  "envisagedCommodityCode": "$randomString",
    |  "knownLegalProceedings": "$randomString",
    |  "confidentialInformation": "$randomString"
    |},
    |"attachments": []
    }""".stripMargin
}
