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

import play.api.libs.json.Json

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class CheckRegisteredDetailsSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with OptionValues {

  "CheckRegisteredDetails" - {
    val validJson = Json.parse("""
                      |{
                      |  "value": true,
                      |  "eori": "GB123456789012",
                      |  "name": "name",
                      |  "streetAndNumber": "streetAndNumber",
                      |  "city": "city",
                      |  "country": "country",
                      |  "postalCode": "postalCode",
                      |  "phoneNumber": "phoneNumber"
                      |}
      """.stripMargin)

    val validDetails = CheckRegisteredDetails(
      value = true,
      eori = "GB123456789012",
      name = "name",
      streetAndNumber = "streetAndNumber",
      city = "city",
      country = "country",
      postalCode = Some("postalCode"),
      phoneNumber = Some("phoneNumber")
    )

    "must deserialise" in {
      CheckRegisteredDetails.format.reads(validJson).asOpt mustEqual Some(validDetails)
    }
    "must serialise" in {
      CheckRegisteredDetails.format.writes(validDetails) mustEqual validJson
    }
    "must serialise and deserialise" in {
      val gen = arbitrary[CheckRegisteredDetails]

      forAll(gen) {
        checkRegisteredDetails =>
          val asJson = CheckRegisteredDetails.format.writes(checkRegisteredDetails)
          val result = CheckRegisteredDetails.format.reads(asJson).asOpt

          result mustEqual Some(checkRegisteredDetails)
      }
    }
  }
}
