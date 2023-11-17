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

import play.api.mvc.PathBindable

import base.SpecBase
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class IndexSpec extends SpecBase with EitherValues with ScalaCheckPropertyChecks {

  private val pathBindable = implicitly[PathBindable[Index]]

  "Index" - {

    "must bind positive integers to an index that is one less than the value" in {

      forAll(Gen.choose(1, Int.MaxValue)) { number =>
        pathBindable.bind("key", number.toString).value mustEqual Index(number - 1)
      }
    }

    "must not bind from negative numbers or 0" in {

      forAll(Gen.choose(Int.MinValue, 0)) { number =>
        pathBindable.bind("key", number.toString) mustBe a[Left[_, Index]]
      }
    }

    "must unbind to a number 1 greater than the index" in {

      forAll(Gen.choose(0, Int.MaxValue - 1)) { number =>
        pathBindable.unbind("key", Index(number)) mustEqual (number + 1).toString
      }
    }

    "+" - {

      "must return an index with a position equal to this index's position plus the new amount" in {

        forAll(Gen.choose(0, 100), Gen.choose(0, 100)) { case (original, additional) =>
          Index(original) + additional mustEqual Index(original + additional)
        }
      }
    }
  }
}
