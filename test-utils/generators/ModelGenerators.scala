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

package generators

import models._
import models.requests.ApplicationId
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryCDSEstablishmentAddress: Arbitrary[CDSEstablishmentAddress] =
    Arbitrary {
      for {
        street      <- arbitrary[String]
        city        <- arbitrary[String]
        countryCode <- Gen.stringOfN(2, arbitrary[Char])
        postalOpt   <- Gen.option(Gen.stringOfN(7, arbitrary[Char]))
      } yield CDSEstablishmentAddress(
        street,
        city,
        countryCode,
        postalOpt
      )
    }

  implicit lazy val arbitraryContactInformation: Arbitrary[ContactInformation] =
    Arbitrary {
      for {
        personOfContact <- arbitrary[Option[String]]
        sca             <- arbitrary[Option[Boolean]]
        street          <- arbitrary[Option[String]]
        city            <- arbitrary[Option[String]]
        postalOpt       <- Gen.option(Gen.stringOfN(7, arbitrary[Char]))
        countryCode     <- Gen.option(Gen.stringOfN(2, arbitrary[Char]))
        tNo             <- Gen.option(Gen.stringOfN(11, Gen.numChar))
        email           <- Gen.option(Gen.oneOf(Seq("test@test.com", "test2@test.com")))
        eTime           <- arbitrary[Option[String]]
      } yield ContactInformation(
        personOfContact,
        sca,
        street,
        city,
        postalOpt,
        countryCode,
        tNo,
        tNo,
        email,
        eTime
      )
    }

  implicit lazy val arbitraryTraderDetailsWithConfirmation: Arbitrary[TraderDetailsWithConfirmation] =
    Arbitrary {
      for {
        eoriNo       <- arbitrary[String]
        consent      <- arbitrary[Boolean]
        name         <- arbitrary[String]
        address      <- arbitrary[CDSEstablishmentAddress]
        contact      <- arbitrary[Option[ContactInformation]]
        confirmation <- arbitrary[Option[Boolean]]
      } yield TraderDetailsWithConfirmation(
        eoriNo,
        consent,
        name,
        address,
        contact,
        confirmation
      )
    }

  implicit lazy val arbitraryAgentCompanyDetails: Arbitrary[AgentCompanyDetails] =
    Arbitrary {
      for {
        agentEori            <- arbitrary[String]
        agentCompanyName     <- arbitrary[String]
        agentStreetAndNumber <- arbitrary[String]
        agentCity            <- arbitrary[String]
        agentCountry         <- Gen.oneOf(Country.allCountries)
        agentPostalCode      <- arbitrary[Option[String]]

      } yield AgentCompanyDetails(
        agentEori,
        agentCompanyName,
        agentStreetAndNumber,
        agentCity,
        agentCountry,
        agentPostalCode
      )
    }

  implicit lazy val arbitraryWhatIsYourRoleAsImporter: Arbitrary[WhatIsYourRoleAsImporter] =
    Arbitrary {
      Gen.oneOf(WhatIsYourRoleAsImporter.values)
    }

  implicit lazy val arbitraryAdaptMethod: Arbitrary[AdaptMethod] =
    Arbitrary {
      Gen.oneOf(AdaptMethod.values)
    }

  implicit lazy val arbitraryRequiredInformation: Arbitrary[RequiredInformation] =
    Arbitrary {
      Gen.oneOf(RequiredInformation.values)
    }

  implicit lazy val arbitraryValuationMethod: Arbitrary[ValuationMethod] =
    Arbitrary {
      Gen.oneOf(ValuationMethod.values)
    }

  implicit lazy val applicationIdGen: Gen[ApplicationId] =
    for {
      value <- Gen.choose(1, 999999999)
    } yield ApplicationId(value)
}
