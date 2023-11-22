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

import generators.ApplicationRequestGenerator
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.mvc.PathBindable

class DraftIdSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator
    with EitherValues
    with OptionValues {

  "a draft Id" - {

    val pathBindable = implicitly[PathBindable[DraftId]]

    "must bind from a url" in {

      forAll(arbitrary[String], arbitrary[DraftId]) { (key, value) =>
        pathBindable.bind(key, value.toString).value mustEqual value
      }
    }

    "must not bind from a url" in {
      pathBindable.bind("key", "test").left.value mustEqual "Invalid draft Id"
    }

    "must unbind to a url" in {

      forAll(arbitrary[String], arbitrary[DraftId]) { (key, value) =>
        pathBindable.unbind(key, value) mustEqual value.toString
      }
    }

    "must serialise and deserialise to / from JSON" in {

      forAll(arbitrary[DraftId]) { draftId =>
        val json = Json.toJson(draftId)
        json mustEqual JsString(draftId.toString)
        json.validate[DraftId] mustEqual JsSuccess(draftId)
      }
    }
  }

  "fromString" - {
    "must return a DraftId when given a valid string" in {
      val draftId = DraftId.fromString("DRAFT123456789").value
      draftId mustBe a[DraftId]
    }

    "must throw an exception when given an invalid string" in {
      DraftId.fromString("123456789") mustBe None
    }
  }
}
