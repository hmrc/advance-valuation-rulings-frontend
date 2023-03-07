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

package generators

import play.api.http.Status

import models.{AcknowledgementReference, BackendError, CDSEstablishmentAddress, EoriNumber, TraderDetailsWithCountryCode}
import org.scalacheck.{Arbitrary, Gen}
import wolfendale.scalacheck.regexp.RegexpGen

trait TraderDetailsGenerator extends Generators {

  implicit lazy val arbitraryEoriNumberGen: Arbitrary[EoriNumber] = Arbitrary(
    RegexpGen.from("^[A-Z]{2}[0-9A-Z]{12}$").map(EoriNumber)
  )

  implicit lazy val arbitraryTraderDetailsWithCountryCode: Arbitrary[TraderDetailsWithCountryCode] =
    Arbitrary {
      for {
        eoriNumber      <- arbitraryEoriNumberGen.arbitrary
        cdsFullName     <- stringsWithMaxLength(512)
        streetAndNumber <- stringsWithMaxLength(70)
        city            <- stringsWithMaxLength(35)
        country         <- stringsWithMaxLength(2)
        postalCode      <- Gen.option(stringsWithMaxLength(9))
      } yield TraderDetailsWithCountryCode(
        eoriNumber.value,
        cdsFullName,
        CDSEstablishmentAddress(streetAndNumber, city, country, postalCode)
      )
    }

  implicit lazy val arbitraryAcknowledgementReferenceGen: Arbitrary[AcknowledgementReference] =
    Arbitrary(stringsWithMaxLength(32).map(AcknowledgementReference))

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
