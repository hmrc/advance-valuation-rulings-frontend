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

import play.api.libs.json.{Json, JsSuccess}

import generators._
import models.ApplicationNumber
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
          applicationNumber = applicationNumber,
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

object ApplicationRequestSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val applicationNumber: String = ApplicationNumber("GBAVR", 1).render

  val applicant = IndividualApplicant(
    holder = EORIDetails(
      eori = randomString,
      businessName = randomString,
      addressLine1 = randomString,
      addressLine2 = randomString,
      addressLine3 = "",
      postcode = randomString,
      country = randomString
    ),
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
    goodDescription = randomString,
    envisagedCommodityCode = Some(randomString),
    knownLegalProceedings = Some(randomString),
    confidentialInformation = Some(randomString)
  )

  val body =
    s"""{
    |"applicationNumber": "$applicationNumber",
    |"applicant": {
    |  "holder": {
    |    "eori": "$randomString",
    |    "businessName": "$randomString",
    |    "addressLine1": "$randomString",
    |    "addressLine2": "$randomString",
    |    "addressLine3": "",
    |    "postcode": "$randomString",
    |    "country": "$randomString"
    |  },
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
    |  "goodDescription": "$randomString",
    |  "envisagedCommodityCode": "$randomString",
    |  "knownLegalProceedings": "$randomString",
    |  "confidentialInformation": "$randomString"
    |},
    |"attachments": []
    }""".stripMargin
}
