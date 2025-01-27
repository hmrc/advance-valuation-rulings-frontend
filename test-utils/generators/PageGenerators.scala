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

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryAgentCompanyDetailsPage: Arbitrary[AgentCompanyDetailsPage.type] =
    Arbitrary(AgentCompanyDetailsPage)

  implicit lazy val arbitraryBusinessContactDetailsPage: Arbitrary[BusinessContactDetailsPage.type] =
    Arbitrary(BusinessContactDetailsPage)

  implicit lazy val arbitraryWhatIsYourRoleAsImporterPage: Arbitrary[WhatIsYourRoleAsImporterPage.type] =
    Arbitrary(WhatIsYourRoleAsImporterPage)

  implicit lazy val arbitraryHaveYouUsedMethodOneForSimilarGoodsInPastPage
    : Arbitrary[HaveYouUsedMethodOneForSimilarGoodsInPastPage.type] =
    Arbitrary(HaveYouUsedMethodOneForSimilarGoodsInPastPage)

  implicit lazy val arbitraryDescribeTheSimilarGoodsPage: Arbitrary[DescribeTheSimilarGoodsPage.type] =
    Arbitrary(DescribeTheSimilarGoodsPage)

  implicit lazy val arbitraryExplainWhyYouHaveNotSelectedMethodOneToFivePage
    : Arbitrary[ExplainWhyYouHaveNotSelectedMethodOneToFivePage.type] =
    Arbitrary(ExplainWhyYouHaveNotSelectedMethodOneToFivePage)

  implicit lazy val arbitraryExplainHowYouWillUseMethodSixPage: Arbitrary[ExplainHowYouWillUseMethodSixPage.type] =
    Arbitrary(ExplainHowYouWillUseMethodSixPage)

  implicit lazy val arbitraryAdaptMethodPage: Arbitrary[AdaptMethodPage.type] =
    Arbitrary(AdaptMethodPage)

  implicit lazy val arbitraryExplainWhyYouHaveNotSelectedMethodOneToThreePage
    : Arbitrary[ExplainWhyYouHaveNotSelectedMethodOneToThreePage.type] =
    Arbitrary(ExplainWhyYouHaveNotSelectedMethodOneToThreePage)

  implicit lazy val arbitraryExplainWhyYouChoseMethodFourPage: Arbitrary[ExplainWhyYouChoseMethodFourPage.type] =
    Arbitrary(ExplainWhyYouChoseMethodFourPage)

  implicit lazy val arbitraryDescribeTheIdenticalGoodsPage: Arbitrary[DescribeTheIdenticalGoodsPage.type] =
    Arbitrary(DescribeTheIdenticalGoodsPage)

  implicit lazy val arbitraryHaveTheGoodsBeenSubjectToLegalChallengesPage
    : Arbitrary[HaveTheGoodsBeenSubjectToLegalChallengesPage.type] =
    Arbitrary(HaveTheGoodsBeenSubjectToLegalChallengesPage)

  implicit lazy val arbitraryDescriptionOfGoodsPage: Arbitrary[DescriptionOfGoodsPage.type] =
    Arbitrary(DescriptionOfGoodsPage)

  implicit lazy val arbitraryDescribeTheLegalChallengesPage: Arbitrary[DescribeTheLegalChallengesPage.type] =
    Arbitrary(DescribeTheLegalChallengesPage)

  implicit lazy val arbitraryIsThereASaleInvolvedPage: Arbitrary[IsThereASaleInvolvedPage.type] =
    Arbitrary(IsThereASaleInvolvedPage)

  implicit lazy val arbitraryIsSaleBetweenRelatedPartiesPage: Arbitrary[IsSaleBetweenRelatedPartiesPage.type] =
    Arbitrary(IsSaleBetweenRelatedPartiesPage)

  implicit lazy val arbitraryExplainHowPartiesAreRelatedPage: Arbitrary[ExplainHowPartiesAreRelatedPage.type] =
    Arbitrary(ExplainHowPartiesAreRelatedPage)

  implicit lazy val arbitraryIsTheSaleSubjectToConditionsPage: Arbitrary[IsTheSaleSubjectToConditionsPage.type] =
    Arbitrary(IsTheSaleSubjectToConditionsPage)

  implicit lazy val arbitraryDescribeTheRestrictionsPage: Arbitrary[DescribeTheRestrictionsPage.type] =
    Arbitrary(DescribeTheRestrictionsPage)

  implicit lazy val arbitraryDescribeTheConditionsPage: Arbitrary[DescribeTheConditionsPage.type] =
    Arbitrary(DescribeTheConditionsPage)

  implicit lazy val arbitraryAreThereRestrictionsOnTheGoodsPage: Arbitrary[AreThereRestrictionsOnTheGoodsPage.type] =
    Arbitrary(AreThereRestrictionsOnTheGoodsPage)

  implicit lazy val arbitraryWhyTransactionValueOfSimilarGoodsPage
    : Arbitrary[WhyTransactionValueOfSimilarGoodsPage.type] =
    Arbitrary(WhyTransactionValueOfSimilarGoodsPage)

  implicit lazy val arbitraryHaveYouUsedMethodOneInPastPage: Arbitrary[HaveYouUsedMethodOneInPastPage.type] =
    Arbitrary(HaveYouUsedMethodOneInPastPage)

  implicit lazy val arbitraryWhyIdenticalGoodsPage: Arbitrary[WhyIdenticalGoodsPage.type] =
    Arbitrary(WhyIdenticalGoodsPage)

  implicit lazy val arbitraryWhyComputedValuePage: Arbitrary[WhyComputedValuePage.type] =
    Arbitrary(WhyComputedValuePage)

  implicit lazy val arbitraryExplainReasonComputedValuePage: Arbitrary[ExplainReasonComputedValuePage.type] =
    Arbitrary(ExplainReasonComputedValuePage)

  implicit lazy val arbitraryUploadAnotherSupportingDocumentPage: Arbitrary[UploadAnotherSupportingDocumentPage.type] =
    Arbitrary(UploadAnotherSupportingDocumentPage)

  implicit lazy val arbitraryIsThisFileConfidentialPage: Arbitrary[IsThisFileConfidentialPage.type] =
    Arbitrary(IsThisFileConfidentialPage)

  implicit lazy val arbitraryDoYouWantToUploadDocumentsPage: Arbitrary[DoYouWantToUploadDocumentsPage.type] =
    Arbitrary(DoYouWantToUploadDocumentsPage)

  implicit lazy val arbitraryApplicationContactDetailsPage: Arbitrary[ApplicationContactDetailsPage.type] =
    Arbitrary(ApplicationContactDetailsPage)

  implicit lazy val arbitraryCheckRegisteredDetailsPage: Arbitrary[CheckRegisteredDetailsPage.type] =
    Arbitrary(CheckRegisteredDetailsPage)

  implicit lazy val arbitraryHasConfidentialInformationPage: Arbitrary[HasConfidentialInformationPage.type] =
    Arbitrary(HasConfidentialInformationPage)

  implicit lazy val arbitraryConfidentialInformationPage: Arbitrary[ConfidentialInformationPage.type] =
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
}
