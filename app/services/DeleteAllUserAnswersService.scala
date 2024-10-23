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

package services

import models.UserAnswers
import pages._

class DeleteAllUserAnswersService {

  private def allPages: Seq[QuestionPage[_]] =
    Seq(
      AboutSimilarGoodsPage,
      AccountHomePage,
      AdaptMethodPage,
      AgentCompanyDetailsPage,
      AgentForOrgApplicationContactDetailsPage,
      AgentForOrgCheckRegisteredDetailsPage,
      AgentForTraderCheckRegisteredDetailsPage,
      AgentForTraderContactDetailsPage,
      ApplicationContactDetailsPage,
      AreThereRestrictionsOnTheGoodsPage,
      AwareOfRulingPage,
      BusinessContactDetailsPage,
      ChangeYourRoleImporterPage,
      CheckRegisteredDetailsPage,
      ChoosingMethodPage,
      CommodityCodePage,
      ConfidentialInformationPage,
      DescribeTheConditionsPage,
      DescribeTheIdenticalGoodsPage,
      DescribeTheLegalChallengesPage,
      DescribeTheRestrictionsPage,
      DescribeTheSimilarGoodsPage,
      DescriptionOfGoodsPage,
      DoYouWantToUploadDocumentsPage,
      DraftWhatIsYourRoleAsImporterPage,
      EORIBeUpToDatePage,
      ExplainHowPartiesAreRelatedPage,
      ExplainHowYouWillUseMethodSixPage,
      ExplainReasonComputedValuePage,
      ExplainWhyYouChoseMethodFourPage,
      ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
      ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
      HasCommodityCodePage,
      HasConfidentialInformationPage,
      HaveTheGoodsBeenSubjectToLegalChallengesPage,
      HaveYouReceivedADecisionPage,
      HaveYouUsedMethodOneForSimilarGoodsInPastPage,
      HaveYouUsedMethodOneInPastPage,
      ImportGoodsPage,
      IsSaleBetweenRelatedPartiesPage,
      IsThereASaleInvolvedPage,
      IsTheSaleSubjectToConditionsPage,
      IsThisFileConfidentialPage,
      ProvideTraderEoriPage,
      RequiredInformationPage,
      TellUsAboutYourRulingPage,
      UploadAnotherSupportingDocumentPage,
      UploadLetterOfAuthorityPage,
      UploadSupportingDocumentPage,
      ValuationMethodPage, // Note: if the user answers for ValuationMethodPage are removed it will remove AdaptMethodPage
      VerifyLetterOfAuthorityPage,
      VerifyTraderDetailsPage,
      WhatIsYourRoleAsImporterPage,
      WhyComputedValuePage,
      WhyIdenticalGoodsPage,
      WhyTransactionValueOfSimilarGoodsPage
    )

  def deleteAllUserAnswers(userAnswers: UserAnswers): Option[UserAnswers] =
    allPages.foldLeft[Option[UserAnswers]](Some(userAnswers)) { (userAnswers, pageToRemove) =>
      userAnswers.flatMap(_.remove(pageToRemove).toOption)
    }

  def deleteAllUserAnswersExcept(userAnswers: UserAnswers, excludedPages: Seq[QuestionPage[_]]): Option[UserAnswers] =
    allPages.diff(excludedPages).foldLeft[Option[UserAnswers]](Some(userAnswers)) { (userAnswers, pageToRemove) =>
      userAnswers.flatMap(_.remove(pageToRemove).toOption)
    }

}
