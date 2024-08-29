/*
 * Copyright 2024 HM Revenue & Customs
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

package pages

import models.Index
import org.scalacheck.{Arbitrary, Gen}
import pages.behaviours.PageBehaviours

class RemoveSupportingDocumentPageSpec extends PageBehaviours {

  private val arbitraryIndex: Arbitrary[Index] = Arbitrary {
    Gen.choose(0, 100).map(Index(_))
  }

  private val removeSupportingDocumentPageGen: Gen[RemoveSupportingDocumentPage] =
    for {
      index <- arbitraryIndex.arbitrary
    } yield RemoveSupportingDocumentPage(index)

  "RemoveSupportingDocumentPage" - {

    "must return the valid toString implementation with a valid index" in {
      forAll(removeSupportingDocumentPageGen) { page =>
        page.toString mustBe "removeSupportingDocument"
        page.index.position must (be >= 0 and be <= 100)
      }
    }
  }
}
