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

package models

import play.api.libs.json.{Json, JsSuccess}

import generators.ApplicationRequestGenerator
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

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
  }
}

object ApplicationRequestSpec {

  val applicant = IndividualApplicant(
    holder = EORIDetails(
      eori = "GB1234567890",
      businessName = "businessName",
      addressLine1 = "addressLine1",
      addressLine2 = "",
      addressLine3 = "",
      postcode = "AA1 1AA",
      country = "GB"
    ),
    contact = ContactDetails(
      name = "John Doe",
      email = "john@doe.com",
      phone = Some("01234567890")
    )
  )

  val requestedMethod = MethodThree(
    whyNotOtherMethods = "whyNotOtherMethods",
    detailedDescription = PreviousSimilarGoods("detailed description of similar goods")
  )

  val goodsDetails = GoodsDetails(
    goodDescription = "Some description",
    envisagedCommodityCode = Some("1234567890"),
    knownLegalProceedings = Some("Some legal proceedings"),
    confidentialInformation = Some("Some confidential information")
  )

  val body =
    """{
    |"applicant": {
    |  "holder": {
    |    "eori": "GB1234567890",
    |    "businessName": "businessName",
    |    "addressLine1": "addressLine1",
    |    "addressLine2": "",
    |    "addressLine3": "",
    |    "postcode": "AA1 1AA",
    |    "country": "GB"
    |  },
    |  "contact": {
    |    "name": "John Doe",
    |    "email": "john@doe.com",
    |    "phone": "01234567890"
    |  },
    |  "_type": "models.IndividualApplicant"
    |},
    |"requestedMethod" : {
    |  "whyNotOtherMethods" : "whyNotOtherMethods",
    |  "detailedDescription" : {
    |    "_value" : "detailed description of similar goods",
    |    "_type" : "models.PreviousSimilarGoods"
    |  },
    |  "_type" : "models.MethodThree"
    |},
    |"goodsDetails": {
    |  "goodDescription": "Some description",
    |  "envisagedCommodityCode": "1234567890",
    |  "knownLegalProceedings": "Some legal proceedings",
    |  "confidentialInformation": "Some confidential information"
    |},
    |"attachments": []
    }""".stripMargin
}
