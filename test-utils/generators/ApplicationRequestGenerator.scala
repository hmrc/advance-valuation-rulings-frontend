/*
 * Copyright 2025 HM Revenue & Customs
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

import models.requests._
import models.{DraftId, EoriNumber}
import org.scalacheck._

trait ApplicationRequestGenerator extends Generators {
  private implicit lazy val arbitraryEoriNumberGen: Arbitrary[EoriNumber] = Arbitrary(
    eoriGenerator.map(models.EoriNumber.apply)
  )

  implicit lazy val arbitraryContactDetails: Arbitrary[ContactDetails] = Arbitrary {
    for {
      contactName        <- stringsWithMaxLength(70)
      contactEmail       <- stringsWithMaxLength(70)
      contactTelephone   <- Gen.option(stringsWithMaxLength(9))
      contactCompanyName <- Gen.option(stringsWithMaxLength(70))
      contactJobTitle    <- Gen.option(stringsWithMaxLength(70))
    } yield ContactDetails(
      name = contactName,
      email = contactEmail,
      phone = contactTelephone,
      companyName = contactCompanyName,
      jobTitle = contactJobTitle
    )
  }

  implicit lazy val arbitraryTraderDetail: Arbitrary[TraderDetail] = Arbitrary {
    for {
      eori         <- arbitraryEoriNumberGen.arbitrary
      businessName <- stringsWithMaxLength(100)
      addressLine1 <- stringsWithMaxLength(100)
      addressLine2 <- Gen.option(stringsWithMaxLength(100))
      addressLine3 <- Gen.option(stringsWithMaxLength(100))
      postCode     <- stringsWithMaxLength(10)
      countryCode  <- Gen.oneOf("UK", "JP", "FR", "DE", "IT", "ES", "US")
      phoneNumber  <- Gen.option(stringsWithMaxLength(25))
      isPrivate    <- Gen.option(Gen.oneOf(true, false))
    } yield TraderDetail(
      eori.value,
      businessName,
      addressLine1,
      addressLine2,
      addressLine3,
      postCode,
      countryCode,
      phoneNumber,
      isPrivate
    )
  }

  implicit lazy val arbitraryDraftId: Arbitrary[DraftId] = Arbitrary {
    Gen.choose(1, 999999999).map(DraftId(_))
  }

  implicit lazy val arbitraryGoodsDetails: Arbitrary[GoodsDetails] = Arbitrary {
    for {
      goodsDescription        <- stringsWithMaxLength(100)
      similarGoods            <- Gen.option(stringsWithMaxLength(100))
      similarMethods          <- Gen.option(stringsWithMaxLength(100))
      envisagedCommodityCode  <- Gen.option(stringsWithMaxLength(10))
      knownLegalProceedings   <- Gen.option(stringsWithMaxLength(100))
      confidentialInformation <- Gen.option(stringsWithMaxLength(100))
    } yield GoodsDetails(
      goodsDescription,
      envisagedCommodityCode,
      knownLegalProceedings,
      confidentialInformation,
      similarGoods,
      similarMethods
    )
  }

  implicit lazy val arbitraryPreviousIdenticalGoods: Arbitrary[PreviousIdenticalGoods] = Arbitrary {
    for {
      value <- stringsWithMaxLength(100)
    } yield PreviousIdenticalGoods(value)
  }

  implicit lazy val arbitraryPreviousSimilarGoods: Arbitrary[PreviousSimilarGoods] = Arbitrary {
    for {
      value <- stringsWithMaxLength(100)
    } yield PreviousSimilarGoods(value)
  }

  implicit lazy val arbitraryMethodOne: Arbitrary[MethodOne]     = Arbitrary {
    for {
      saleBetweenRelatedParties <- Gen.option(stringsWithMaxLength(100))
      goodsRestrictions         <- Gen.option(stringsWithMaxLength(100))
      saleConditions            <- Gen.option(stringsWithMaxLength(100))
    } yield MethodOne(saleBetweenRelatedParties, goodsRestrictions, saleConditions)
  }
  implicit lazy val arbitraryMethodTwo: Arbitrary[MethodTwo]     = Arbitrary {
    for {
      whyNotOtherMethods <- stringsWithMaxLength(100)
      identicalGoods     <- arbitraryPreviousIdenticalGoods.arbitrary
    } yield MethodTwo(whyNotOtherMethods, identicalGoods)
  }
  implicit lazy val arbitraryMethodThree: Arbitrary[MethodThree] = Arbitrary {
    for {
      whyNotOtherMethods <- stringsWithMaxLength(100)
      similarGoods       <- arbitraryPreviousSimilarGoods.arbitrary
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

  implicit lazy val arbitraryAttachmentRequest: Arbitrary[AttachmentRequest] = Arbitrary {
    for {
      name        <- alphaStringsWithMaxLength(36)
      description <- Gen.option(stringsWithMaxLength(100))
      url         <- stringsWithMaxLength(100)
      public      <- Gen.oneOf(Privacy.Public, Privacy.Confidential)
      mimeType    <- Gen.oneOf(
                       "application/pdf",
                       "application/msword",
                       "application/vnd.ms-excel",
                       "image/jpeg",
                       "image/png",
                       "text/plain"
                     )
      size        <- Gen.choose(1L, 1000000000000L)
    } yield AttachmentRequest(name, description, url, public, mimeType, size)
  }

  implicit lazy val arbitraryApplicationRequest: Arbitrary[ApplicationRequest] = Arbitrary {
    for {
      draftId           <- arbitraryDraftId.arbitrary
      traderDetail      <- arbitraryTraderDetail.arbitrary
      contact           <- arbitraryContactDetails.arbitrary
      goodsDetails      <- arbitraryGoodsDetails.arbitrary
      method            <- Gen.oneOf(
                             arbitraryMethodOne.arbitrary,
                             arbitraryMethodTwo.arbitrary,
                             arbitraryMethodThree.arbitrary,
                             arbitraryMethodFour.arbitrary,
                             arbitraryMethodFive.arbitrary,
                             arbitraryMethodSix.arbitrary
                           )
      numAttachments    <- Gen.choose(0, 5)
      attachments       <- Gen.listOfN(numAttachments, arbitraryAttachmentRequest.arbitrary)
      whatIsYourRole    <- Gen.oneOf(
                             WhatIsYourRole.AgentOrg,
                             WhatIsYourRole.EmployeeOrg,
                             WhatIsYourRole.AgentTrader
                           )
      letterOfAuthority <- Gen.option(arbitraryAttachmentRequest.arbitrary)
    } yield ApplicationRequest(
      draftId,
      traderDetail,
      None,
      contact,
      method,
      goodsDetails,
      attachments,
      whatIsYourRole,
      letterOfAuthority
    )
  }
}
