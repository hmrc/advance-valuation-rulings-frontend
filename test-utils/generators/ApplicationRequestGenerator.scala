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

import models.{ApplicationNumber, EoriNumber}
import models.requests._
import org.scalacheck._
import wolfendale.scalacheck.regexp.RegexpGen

trait ApplicationRequestGenerator extends Generators {
  private implicit lazy val arbitraryEoriNumberGen: Arbitrary[EoriNumber] = Arbitrary(
    RegexpGen.from("^[A-Z]{2}[0-9A-Z]{12}$").map(EoriNumber)
  )

  implicit lazy val arbitraryUploadedDocument: Arbitrary[UploadedDocument] = Arbitrary {
    for {
      id       <- alphaStringsWithMaxLength(36)
      fileName <- stringsWithMaxLength(100)
      url      <- stringsWithMaxLength(100)
      public   <- Gen.oneOf(true, false)
      mimeType <- Gen.oneOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.ms-excel",
                    "image/jpeg",
                    "image/png",
                    "text/plain"
                  )
      size     <- Gen.choose(1L, 1000000000000L)
    } yield UploadedDocument(id, fileName, url, public, mimeType, size)
  }

  implicit lazy val arbitraryContactDetails: Arbitrary[ContactDetails] = Arbitrary {
    for {
      contactName      <- stringsWithMaxLength(70)
      contactEmail     <- stringsWithMaxLength(70)
      contactTelephone <- Gen.option(stringsWithMaxLength(9))
    } yield ContactDetails(contactName, contactEmail, contactTelephone)
  }

  implicit lazy val arbitraryEoriDetails: Arbitrary[EORIDetails] = Arbitrary {
    for {
      eori         <- arbitraryEoriNumberGen.arbitrary
      businessName <- stringsWithMaxLength(100)
      addressLine1 <- stringsWithMaxLength(100)
      addressLine2 <- stringsWithMaxLength(100)
      addressLine3 <- stringsWithMaxLength(100)
      postCode     <- stringsWithMaxLength(10)
      countryCode  <- Gen.oneOf("UK", "JP", "FR", "DE", "IT", "ES", "US")
    } yield EORIDetails(
      eori.value,
      businessName,
      addressLine1,
      addressLine2,
      addressLine3,
      postCode,
      countryCode
    )
  }

  implicit lazy val arbitraryApplicationNumber: Arbitrary[ApplicationNumber] = Arbitrary {
    intsBelowValue(Int.MaxValue).map(ApplicationNumber(prefix = "GBAVR", _))
  }

  implicit lazy val arbitraryIndividualApplicant: Arbitrary[IndividualApplicant] = Arbitrary {
    for {
      eori           <- arbitraryEoriDetails.arbitrary
      contactDetails <- arbitraryContactDetails.arbitrary
    } yield IndividualApplicant(eori, contactDetails)
  }

  implicit lazy val arbitraryGoodsDetails: Arbitrary[GoodsDetails] = Arbitrary {
    for {
      goodDescription         <- stringsWithMaxLength(100)
      envisagedCommodityCode  <- Gen.option(stringsWithMaxLength(10))
      knownLegalProceedings   <- Gen.option(stringsWithMaxLength(100))
      confidentialInformation <- Gen.option(stringsWithMaxLength(100))
    } yield GoodsDetails(
      goodDescription,
      envisagedCommodityCode,
      knownLegalProceedings,
      confidentialInformation
    )
  }

  implicit lazy val arbitraryOtherUsersIdenticalGoods: Arbitrary[OtherUsersIdenticalGoods] =
    Arbitrary {
      for {
        value <- stringsWithMaxLength(100)
      } yield OtherUsersIdenticalGoods(value)
    }
  implicit lazy val arbitraryPreviousIdenticalGoods: Arbitrary[PreviousIdenticalGoods]     = Arbitrary {
    for {
      value <- stringsWithMaxLength(100)
    } yield PreviousIdenticalGoods(value)
  }

  implicit lazy val arbitraryOtherUsersSimilarGoods: Arbitrary[OtherUsersSimilarGoods] = Arbitrary {
    for {
      value <- stringsWithMaxLength(100)
    } yield OtherUsersSimilarGoods(value)
  }
  implicit lazy val arbitraryPreviousSimilarGoods: Arbitrary[PreviousSimilarGoods]     = Arbitrary {
    for {
      value <- stringsWithMaxLength(100)
    } yield PreviousSimilarGoods(value)
  }

  implicit lazy val arbitrarySimilarGoodsExplanation: Gen[SimilarGoodsExplaination] =
    Gen.oneOf(
      arbitraryOtherUsersSimilarGoods.arbitrary,
      arbitraryPreviousSimilarGoods.arbitrary
    )

  implicit lazy val arbitraryMethodOne: Arbitrary[MethodOne]     = Arbitrary {
    for {
      saleBetweenRelatedParties <- Gen.option(stringsWithMaxLength(100))
      goodsRestrictions         <- Gen.option(stringsWithMaxLength(100))
      saleConditions            <- Gen.option(stringsWithMaxLength(100))
    } yield MethodOne(saleBetweenRelatedParties, goodsRestrictions, saleConditions)
  }
  implicit lazy val arbitraryMethodTwo: Arbitrary[MethodTwo]     = Arbitrary {
    for {
      whyNotOtherMethods  <- stringsWithMaxLength(100)
      prevIdenticalGoods  <- arbitraryPreviousIdenticalGoods.arbitrary
      otherIdenticalGoods <- arbitraryOtherUsersIdenticalGoods.arbitrary
      identicalGoods      <- Gen.oneOf(prevIdenticalGoods, otherIdenticalGoods)
    } yield MethodTwo(whyNotOtherMethods, identicalGoods)
  }
  implicit lazy val arbitraryMethodThree: Arbitrary[MethodThree] = Arbitrary {
    for {
      whyNotOtherMethods <- stringsWithMaxLength(100)

      similarGoods <- arbitrarySimilarGoodsExplanation
    } yield MethodThree(whyNotOtherMethods, similarGoods)
  }

  implicit lazy val arbitraryMethodFour: Arbitrary[MethodFour] = Arbitrary {
    for {
      whyNotOtherMethods <- stringsWithMaxLength(100)
      deductiveMethod    <- stringsWithMaxLength(100)
    } yield MethodFour(whyNotOtherMethods, deductiveMethod)
  }

  implicit lazy val arbitraryMethodFive: Arbitrary[MethodFive] = Arbitrary {
    for {
      whyNotOtherMethods <- stringsWithMaxLength(100)
      computedValue      <- stringsWithMaxLength(100)
    } yield MethodFive(whyNotOtherMethods, computedValue)
  }

  implicit lazy val arbitraryMethodSix: Arbitrary[MethodSix] = Arbitrary {
    for {
      whyNotOtherMethods   <- stringsWithMaxLength(100)
      valuationDescription <- stringsWithMaxLength(100)
      adaptedMethod        <- Gen
                                .oneOf(
                                  AdaptedMethod.MethodOne,
                                  AdaptedMethod.MethodTwo,
                                  AdaptedMethod.MethodThree,
                                  AdaptedMethod.MethodFour,
                                  AdaptedMethod.MethodFive,
                                  AdaptedMethod.Unable
                                )
    } yield MethodSix(whyNotOtherMethods, adaptedMethod, valuationDescription)
  }

  implicit lazy val arbitraryApplicationRequest: Arbitrary[ApplicationRequest] = Arbitrary {
    for {
      applicationNumber <- arbitraryApplicationNumber.arbitrary
      applicant         <- arbitraryIndividualApplicant.arbitrary
      goodsDetails      <- arbitraryGoodsDetails.arbitrary
      method            <- Gen.oneOf(
                             arbitraryMethodOne.arbitrary,
                             arbitraryMethodTwo.arbitrary,
                             arbitraryMethodThree.arbitrary,
                             arbitraryMethodFour.arbitrary,
                             arbitraryMethodFive.arbitrary,
                             arbitraryMethodSix.arbitrary
                           )
      numAttachments    <- Gen.choose(0, 10)
      attachments       <- Gen.listOfN(numAttachments, arbitraryUploadedDocument.arbitrary)
    } yield ApplicationRequest(
      applicationNumber.render,
      applicant,
      method,
      goodsDetails,
      attachments
    )
  }
}
