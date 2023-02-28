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

import play.api.libs.json.{Json, JsString}

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

    "must deserialise valid values" in {

      val gen = arbitrary[CheckRegisteredDetails]

      forAll(gen) {
        checkRegisteredDetails =>
          JsString(checkRegisteredDetails.toString)
            .validate[CheckRegisteredDetails]
            .asOpt
            .value mustEqual checkRegisteredDetails
      }
    }

    "must serialise" in {

      val gen = arbitrary[CheckRegisteredDetails]

      forAll(gen) {
        checkRegisteredDetails =>
          Json.toJson(checkRegisteredDetails) mustEqual JsString(checkRegisteredDetails.toString)
      }
    }
  }
}
