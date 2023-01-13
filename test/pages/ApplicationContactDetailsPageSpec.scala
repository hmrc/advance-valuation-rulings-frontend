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

import models.ApplicationContactDetails
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class ApplicationContactDetailsPageSpec extends PageBehaviours {

  "ApplicationContactDetailsPage" - {

    def shortString = (string: String) => string.nonEmpty && string.length <= 100

    implicit val applicationContactDetailsGen = Arbitrary(for {
      name  <- Arbitrary.arbitrary[String].suchThat(shortString)
      phone <- Arbitrary.arbitrary[String].suchThat(shortString)
      email <- Arbitrary.arbitrary[String].suchThat(shortString)
    } yield ApplicationContactDetails(name, phone, email))

    beRetrievable[ApplicationContactDetails](ApplicationContactDetailsPage)

    beSettable[ApplicationContactDetails](ApplicationContactDetailsPage)

    beRemovable[ApplicationContactDetails](ApplicationContactDetailsPage)
  }
}
