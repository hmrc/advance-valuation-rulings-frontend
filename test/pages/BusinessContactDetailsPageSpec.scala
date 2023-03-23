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

package pages

import models.{ApplicationContactDetails, BusinessContactDetails}
import org.scalacheck.{Arbitrary, Gen}
import pages.behaviours.PageBehaviours

class BusinessContactDetailsPageSpec extends PageBehaviours {

  "BusinessContactDetailsPage" - {
    def arbitraryString(gen: Gen[Char], maxSize: Int = 100): Gen[String] =
      Gen.listOfN(maxSize, gen).map(_.mkString)

    implicit val businessContactDetailsGen: Arbitrary[BusinessContactDetails] = Arbitrary(for {

      name    <- arbitraryString(Gen.alphaChar)
      phone   <- arbitraryString(Gen.numChar, maxSize = 24)
      email   <- arbitraryString(Gen.asciiChar)
      company <- arbitraryString(Gen.alphaChar)
    } yield BusinessContactDetails(name, phone, email, company))

    beRetrievable[BusinessContactDetails](BusinessContactDetailsPage)

    beSettable[BusinessContactDetails](BusinessContactDetailsPage)

    beRemovable[BusinessContactDetails](BusinessContactDetailsPage)
  }
}
