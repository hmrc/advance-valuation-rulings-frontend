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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.http.Status
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

trait TraderDetailsGenerator extends Generators {

  implicit lazy val arbitraryEoriNumberGen: Arbitrary[EoriNumber] = Arbitrary(
    eoriGenerator.map(EoriNumber)
  )

  def contactInformationGen: Gen[ContactInformation] = for {
    personOfContact           <- Gen.option(stringsWithMaxLength(70))
    streetAndNumber           <- Gen.option(stringsWithMaxLength(70))
    sepCorrAddrIndicator      <- Gen.option(Gen.oneOf(true, false))
    city                      <- Gen.option(stringsWithMaxLength(35))
    postalCode                <- Gen.option(stringsWithMaxLength(9))
    countryCode               <- Gen.option(stringsWithMaxLength(2))
    telephoneNumber           <- Gen.option(stringsWithMaxLength(50))
    faxNumber                 <- Gen.option(stringsWithMaxLength(50))
    emailAddress              <- Gen.option(stringsWithMaxLength(50))
    instant                   <- Gen.option(localDateTimeGen.map(_.toInstant(ZoneOffset.UTC)))
    emailVerificationTimestamp = instant.map(DateTimeFormatter.ISO_INSTANT.format(_))
  } yield ContactInformation(
    personOfContact,
    sepCorrAddrIndicator,
    streetAndNumber,
    city,
    postalCode,
    countryCode,
    telephoneNumber,
    faxNumber,
    emailAddress,
    emailVerificationTimestamp
  )

  implicit lazy val arbitraryTraderDetailsWithCountryCode: Arbitrary[TraderDetailsWithCountryCode] =
    Arbitrary {
      for {
        eoriNumber                        <- arbitraryEoriNumberGen.arbitrary
        consentToDisclosureOfPersonalData <- arbitrary[Boolean]
        cdsFullName                       <- stringsWithMaxLength(512)
        streetAndNumber                   <- stringsWithMaxLength(70)
        city                              <- stringsWithMaxLength(35)
        country                           <- stringsWithMaxLength(2)
        postalCode                        <- Gen.option(stringsWithMaxLength(9))
        contactInformation                <- Gen.option(contactInformationGen)
      } yield TraderDetailsWithCountryCode(
        eoriNumber.value,
        consentToDisclosureOfPersonalData,
        cdsFullName,
        CDSEstablishmentAddress(streetAndNumber, city, country, postalCode),
        contactInformation
      )
    }

  implicit lazy val arbitraryAcknowledgementReferenceGen: Arbitrary[AcknowledgementReference] =
    Arbitrary(stringsWithMaxLength(32).map(value => AcknowledgementReference(value)))

  private val error5xx = Seq(
    Status.INTERNAL_SERVER_ERROR,
    Status.SERVICE_UNAVAILABLE,
    Status.BAD_GATEWAY,
    Status.GATEWAY_TIMEOUT
  )

  private def error4xx = Seq(
    Status.BAD_REQUEST,
    Status.UNAUTHORIZED,
    Status.FORBIDDEN,
    Status.NOT_FOUND
  )

  implicit lazy val arbitrary5xxBackendError: Arbitrary[BackendError] =
    Arbitrary {
      for {
        code    <- Gen.oneOf(error5xx)
        message <- stringsWithMaxLength(255)
      } yield BackendError(code, message)
    }

  implicit lazy val arbitrary4xxBackendError: Arbitrary[BackendError] =
    Arbitrary {
      for {
        code    <- Gen.oneOf(error4xx)
        message <- stringsWithMaxLength(255)
      } yield BackendError(code, message)
    }
}
