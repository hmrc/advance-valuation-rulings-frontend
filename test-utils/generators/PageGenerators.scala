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

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryIsThereASaleInvolvedPage: Arbitrary[IsThereASaleInvolvedPage.type] =
    Arbitrary(IsThereASaleInvolvedPage)

  implicit lazy val arbitraryIsSaleBetweenRelatedPartiesPage
    : Arbitrary[IsSaleBetweenRelatedPartiesPage.type] =
    Arbitrary(IsSaleBetweenRelatedPartiesPage)

  implicit lazy val arbitraryExplainHowPartiesAreRelatedPage
    : Arbitrary[ExplainHowPartiesAreRelatedPage.type] =
    Arbitrary(ExplainHowPartiesAreRelatedPage)

  implicit lazy val arbitraryIsTheSaleSubjectToConditionsPage
    : Arbitrary[IsTheSaleSubjectToConditionsPage.type] =
    Arbitrary(IsTheSaleSubjectToConditionsPage)

  implicit lazy val arbitraryDescribeTheRestrictionsPage
    : Arbitrary[DescribeTheRestrictionsPage.type] =
    Arbitrary(DescribeTheRestrictionsPage)

  implicit lazy val arbitraryDescribeTheConditionsPage: Arbitrary[DescribeTheConditionsPage.type] =
    Arbitrary(DescribeTheConditionsPage)

  implicit lazy val arbitraryAreThereRestrictionsOnTheGoodsPage
    : Arbitrary[AreThereRestrictionsOnTheGoodsPage.type] =
    Arbitrary(AreThereRestrictionsOnTheGoodsPage)

  implicit lazy val arbitraryWhyTransactionValueOfSimilarGoodsPage
    : Arbitrary[WhyTransactionValueOfSimilarGoodsPage.type] =
    Arbitrary(WhyTransactionValueOfSimilarGoodsPage)

  implicit lazy val arbitraryHaveYouUsedMethodOneInPastPage
    : Arbitrary[HaveYouUsedMethodOneInPastPage.type] =
    Arbitrary(HaveYouUsedMethodOneInPastPage)

  implicit lazy val arbitraryWhyIdenticalGoodsPage: Arbitrary[WhyIdenticalGoodsPage.type] =
    Arbitrary(WhyIdenticalGoodsPage)

  implicit lazy val arbitraryWhyComputedValuePage: Arbitrary[WhyComputedValuePage.type] =
    Arbitrary(WhyComputedValuePage)

  implicit lazy val arbitraryExplainReasonComputedValuePage
    : Arbitrary[ExplainReasonComputedValuePage.type] =
    Arbitrary(ExplainReasonComputedValuePage)

  implicit lazy val arbitraryUploadAnotherSupportingDocumentPage
    : Arbitrary[UploadAnotherSupportingDocumentPage.type] =
    Arbitrary(UploadAnotherSupportingDocumentPage)

  implicit lazy val arbitraryIsThisFileConfidentialPage
    : Arbitrary[IsThisFileConfidentialPage.type] =
    Arbitrary(IsThisFileConfidentialPage)

  implicit lazy val arbitraryDoYouWantToUploadDocumentsPage
    : Arbitrary[DoYouWantToUploadDocumentsPage.type] =
    Arbitrary(DoYouWantToUploadDocumentsPage)

  implicit lazy val arbitraryApplicationContactDetailsPage
    : Arbitrary[ApplicationContactDetailsPage.type] =
    Arbitrary(ApplicationContactDetailsPage)

  implicit lazy val arbitraryWhatCountryAreGoodsFromPage
    : Arbitrary[WhatCountryAreGoodsFromPage.type] =
    Arbitrary(WhatCountryAreGoodsFromPage)

  implicit lazy val arbitraryAreGoodsShippedDirectlyPage
    : Arbitrary[AreGoodsShippedDirectlyPage.type] =
    Arbitrary(AreGoodsShippedDirectlyPage)

  implicit lazy val arbitraryCheckRegisteredDetailsPage
    : Arbitrary[CheckRegisteredDetailsPage.type] =
    Arbitrary(CheckRegisteredDetailsPage)

  implicit lazy val arbitraryPriceOfGoodsPage: Arbitrary[PriceOfGoodsPage.type] =
    Arbitrary(PriceOfGoodsPage)

  implicit lazy val arbitraryHasConfidentialInformationPage
    : Arbitrary[HasConfidentialInformationPage.type] =
    Arbitrary(HasConfidentialInformationPage)

  implicit lazy val arbitraryConfidentialInformationPage
    : Arbitrary[ConfidentialInformationPage.type] =
    Arbitrary(ConfidentialInformationPage)

  implicit lazy val arbitraryRequiredInformationPage: Arbitrary[RequiredInformationPage.type] =
    Arbitrary(RequiredInformationPage)

  implicit lazy val arbitraryImportGoodsPage: Arbitrary[ImportGoodsPage.type] =
    Arbitrary(ImportGoodsPage)

  implicit lazy val arbitraryHasCommodityCodePage: Arbitrary[HasCommodityCodePage.type] =
    Arbitrary(HasCommodityCodePage)

  implicit lazy val arbitraryCommodityCodePage: Arbitrary[CommodityCodePage.type] =
    Arbitrary(CommodityCodePage)

  implicit lazy val arbitraryValuationMethodPage: Arbitrary[ValuationMethodPage.type] =
    Arbitrary(ValuationMethodPage)

  implicit lazy val arbitraryNameOfGoodsPage: Arbitrary[NameOfGoodsPage.type] =
    Arbitrary(NameOfGoodsPage)
}
