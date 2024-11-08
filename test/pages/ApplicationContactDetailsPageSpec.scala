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

import models.ApplicationContactDetails
import org.scalacheck.{Arbitrary, Gen}
import pages.behaviours.PageBehaviours

class ApplicationContactDetailsPageSpec extends PageBehaviours {

  "ApplicationContactDetailsPage" - {
    def arbitraryString(gen: Gen[Char], maxSize: Int = 100): Gen[String] =
      Gen.listOfN(maxSize, gen).map(_.mkString)

    given applicationContactDetailsGen: Arbitrary[ApplicationContactDetails] =
      Arbitrary(for {

        name     <- arbitraryString(Gen.alphaChar)
        phone    <- arbitraryString(Gen.numChar, maxSize = 24)
        email    <- arbitraryString(Gen.asciiChar)
        jobTitle <- arbitraryString(Gen.alphaChar)
      } yield ApplicationContactDetails(name, phone, email, jobTitle))

    beRetrievable[ApplicationContactDetails](ApplicationContactDetailsPage)

    beSettable[ApplicationContactDetails](ApplicationContactDetailsPage)

    beRemovable[ApplicationContactDetails](ApplicationContactDetailsPage)
  }
}
