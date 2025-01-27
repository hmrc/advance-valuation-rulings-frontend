/*
 * Copyright 2025 HM Revenue & Customs
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

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, JsSuccess, JsValue, Json}
import play.api.mvc.PathBindable

class ApplicationIdSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ModelGenerators
    with EitherValues {

  private val invalidApplicationIds: Seq[String] = Seq("GBAVR123", "GBAVR12345678", "GBAVR1234ABCD", "INVALID123456789")

  "an application Id" - {

    val pathBindable: PathBindable[ApplicationId] = implicitly[PathBindable[ApplicationId]]

    "must bind from a url" in {

      forAll(arbitrary[String], applicationIdGen) { (key, value) =>
        pathBindable.bind(key, value.toString).value mustBe value
      }
    }

    "must unbind to a url" in {

      forAll(arbitrary[String], applicationIdGen) { (key, value) =>
        pathBindable.unbind(key, value) mustBe value.toString
      }
    }

    "must serialise and deserialise to / from JSON" in {

      forAll(applicationIdGen) { applicationId =>
        val json: JsValue = Json.toJson(applicationId)
        json mustBe JsString(applicationId.toString)
        json.validate[ApplicationId] mustBe JsSuccess(applicationId)
      }
    }

    "must return None when incorrectly formatted in fromString" in {

      invalidApplicationIds.foreach { invalidApplicationId =>
        ApplicationId.fromString(invalidApplicationId) mustBe None
      }
    }

    "must return Left(\"Invalid application Id\") when invalid in pathBindable" in {

      invalidApplicationIds.foreach { invalidApplicationId =>
        pathBindable.bind("id", invalidApplicationId) mustBe Left("Invalid application Id")
      }
    }
  }
}
