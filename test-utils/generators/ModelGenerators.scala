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

import java.net.URL
import java.time.Instant

import models._
import models.fileupload._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._

trait ModelGenerators {

  private def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, Gen.alphaNumChar)
    } yield chars.mkString

  private def alphaStringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, Gen.alphaChar)
    } yield chars.mkString

  implicit lazy val arbitraryWhatIsYourRoleAsImporter: Arbitrary[WhatIsYourRoleAsImporter] =
    Arbitrary {
      Gen.oneOf(WhatIsYourRoleAsImporter.values.toSeq)
    }

  implicit lazy val arbitraryAdaptMethod: Arbitrary[AdaptMethod] =
    Arbitrary {
      Gen.oneOf(AdaptMethod.values.toSeq)
    }

  implicit lazy val arbitraryRequiredInformation: Arbitrary[RequiredInformation] =
    Arbitrary {
      Gen.oneOf(RequiredInformation.values)
    }

  implicit lazy val arbitraryValuationMethod: Arbitrary[ValuationMethod] =
    Arbitrary {
      Gen.oneOf(ValuationMethod.values.toSeq)
    }

  implicit lazy val arbitraryCheckRegisteredDetails: Arbitrary[CheckRegisteredDetails] = Arbitrary {
    for {
      value           <- arbitrary[Boolean]
      eori            <- Gen.alphaStr.suchThat(_.nonEmpty)
      name            <- Gen.alphaStr.suchThat(_.nonEmpty)
      streetAndNumber <- Gen.alphaStr.suchThat(_.nonEmpty)
      city            <- Gen.alphaStr.suchThat(_.nonEmpty)
      country         <- Gen.alphaStr.suchThat(_.nonEmpty)
      postalCode      <- Gen.option(Gen.alphaStr.suchThat(_.nonEmpty))
    } yield CheckRegisteredDetails(value, eori, name, streetAndNumber, city, country, postalCode)
  }

  implicit lazy val arbitraryCallbackUploadDetails: Arbitrary[CallbackUploadDetails] = Arbitrary {
    for {
      fileName        <- alphaStringsWithMaxLength(10)
      fileMimeType    <- Gen.oneOf(Seq("application/pdf", "image/jpeg"))
      checksum        <- stringsWithMaxLength(10)
      size            <- Gen.posNum[Long]
      uploadTimestamp <- Gen.long.map(Instant.ofEpochMilli)
    } yield CallbackUploadDetails(uploadTimestamp, checksum, fileMimeType, fileName, size)
  }

  def urlGen: Gen[URL] =
    for {
      domain <- Gen.listOfN(3, alphaStringsWithMaxLength(10)).map(_.mkString("."))
      file   <- alphaStringsWithMaxLength(10)
    } yield new URL("https", domain, file)

  implicit lazy val arbitraryReadyCallbackBody: Arbitrary[ReadyCallbackBody] = Arbitrary {
    for {
      reference     <- Gen.alphaStr.suchThat(_.nonEmpty).map(Reference(_))
      downloadUrl   <- urlGen
      uploadDetails <- arbitraryCallbackUploadDetails.arbitrary
    } yield ReadyCallbackBody(reference, downloadUrl, uploadDetails)
  }
}
