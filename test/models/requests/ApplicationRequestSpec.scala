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

    "return invalid for an Organisation when built from empty userAnswers" in {

      val result = ApplicationRequest(emptyUserAnswers, AffinityGroup.Organisation)

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
}

object ApplicationRequestSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val draftId: DraftId = DraftId(1)

  val emptyUserAnswers: UserAnswers = UserAnswers("a", draftId)

  val eoriDetails = TraderDetail(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = randomString,
    countryCode = randomString,
    phoneNumber = Some(randomString)
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

  val body =
    s"""{
    |"draftId": "$draftId",
    |"trader": {
    |  "eori": "$randomString",
    |  "businessName": "$randomString",
    |  "addressLine1": "$randomString",
    |  "addressLine2": "$randomString",
    |  "postcode": "$randomString",
    |  "countryCode": "$randomString",
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
}
