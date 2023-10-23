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

package navigation

import play.api.mvc.Call

import controllers.routes._
import models.{CheckMode, _}
import models.ValuationMethod._
import pages._
import queries.AllDocuments

object CheckModeNavigator {
  import controllers._

  // Pre nav
  private def checkRegisteredDetails(userAnswers: UserAnswers): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None        => CheckRegisteredDetailsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(value) =>
        if (value) {
          CheckYourAnswersController.onPageLoad(userAnswers.draftId)
        } else {
          EORIBeUpToDateController.onPageLoad(userAnswers.draftId)
        }
    }

  // Post navigation
  private def hasConfidentialInformation(userAnswers: UserAnswers): Call =
    userAnswers.get(HasConfidentialInformationPage) match {
      case None        => HasConfidentialInformationController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  =>
        ConfidentialInformationController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(false) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  private def haveBeenSubjectToLegalChallenges(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage) match {
      case None        =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
          CheckMode,
          userAnswers.draftId
        )
      case Some(true)  =>
        DescribeTheLegalChallengesController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(false) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  private def hasCommodityCode(userAnswers: UserAnswers): Call =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  => CommodityCodeController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(false) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  private def whatIsYourRoleAsImporter(userAnswers: UserAnswers): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None                                              =>
        WhatIsYourRoleAsImporterController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg) =>
        AgentCompanyDetailsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(WhatIsYourRoleAsImporter.EmployeeOfOrg)      =>
        CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  private def doYouWantToUploadDocuments(userAnswers: UserAnswers): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  =>
        UploadSupportingDocumentsController
          .onPageLoad(CheckMode, userAnswers.draftId, None, None)
      case Some(false) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  private def uploadSupportingDocumentPage(userAnswers: UserAnswers): Call =
    IsThisFileConfidentialController.onPageLoad(
      CheckMode,
      userAnswers.draftId
    )

  private def isThisFileConfidential(userAnswers: UserAnswers): Call =
    UploadAnotherSupportingDocumentController.onPageLoad(CheckMode, userAnswers.draftId)

  private def uploadAnotherSupportingDocument(userAnswers: UserAnswers): Call =
    userAnswers
      .get(UploadAnotherSupportingDocumentPage)
      .map {
        case true  =>
          UploadSupportingDocumentsController.onPageLoad(
            CheckMode,
            userAnswers.draftId,
            None,
            None
          )
        case false => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

      }
      .getOrElse(JourneyRecoveryController.onPageLoad())

  private def uploadLetterOfAuthority(userAnswers: UserAnswers): Call =
    VerifyLetterOfAuthorityController.onPageLoad(CheckMode, userAnswers.draftId)

  private def removeSupportingDocumentPage(userAnswers: UserAnswers): Call = {
    val numberOfDocuments = userAnswers.get(AllDocuments).map(_.size)

    numberOfDocuments match {
      case Some(count) if count > 0 =>
        UploadAnotherSupportingDocumentController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) | None           =>
        DoYouWantToUploadDocumentsController.onPageLoad(CheckMode, userAnswers.draftId)
    }
  }

  // Valuation Method
  private def valuationMethod(userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None          => ValuationMethodController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(Method1) =>
        IsThereASaleInvolvedController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(Method2) => WhyIdenticalGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(Method3) =>
        WhyTransactionValueOfSimilarGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(Method4) =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(
          CheckMode,
          userAnswers.draftId
        )
      case Some(Method5) => WhyComputedValueController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(Method6) =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(
          CheckMode,
          userAnswers.draftId
        )
    }

  // Method 1----------------------------------------------------------------
  private def isThereASaleInvolved(userAnswers: UserAnswers): Call =
    userAnswers.get(IsThereASaleInvolvedPage) match {
      case None        => IsThereASaleInvolvedController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  => nextPage(IsSaleBetweenRelatedPartiesPage, userAnswers)
      case Some(false) => ValuationMethodController.onPageLoad(CheckMode, userAnswers.draftId)
    }

  private def isSaleBetweenRelatedParties(userAnswers: UserAnswers): Call =
    userAnswers.get(IsSaleBetweenRelatedPartiesPage) match {
      case None        => IsSaleBetweenRelatedPartiesController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  => nextPage(ExplainHowPartiesAreRelatedPage, userAnswers)
      case Some(false) => nextPage(AreThereRestrictionsOnTheGoodsPage, userAnswers)
    }

  private def explainHowPartiesAreRelated(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainHowPartiesAreRelatedPage) match {
      case None    => ExplainHowPartiesAreRelatedController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => nextPage(AreThereRestrictionsOnTheGoodsPage, userAnswers)
    }

  private def areThereRestrictionsOnTheGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(AreThereRestrictionsOnTheGoodsPage) match {
      case None        =>
        AreThereRestrictionsOnTheGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  => nextPage(DescribeTheRestrictionsPage, userAnswers)
      case Some(false) => nextPage(IsTheSaleSubjectToConditionsPage, userAnswers)
    }

  private def explainRestrictionsOnTheGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheRestrictionsPage) match {
      case None    => DescribeTheRestrictionsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => nextPage(IsTheSaleSubjectToConditionsPage, userAnswers)
    }

  private def isTheSaleSubjectToConditions(userAnswers: UserAnswers): Call =
    userAnswers.get(IsTheSaleSubjectToConditionsPage) match {
      case None        => IsTheSaleSubjectToConditionsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  => nextPage(DescribeTheConditionsPage, userAnswers)
      case Some(false) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  private def explainTheConditions(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheConditionsPage) match {
      case None    => DescribeTheConditionsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  // Method 2----------------------------------------------------------------
  private def whyIdenticalGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyIdenticalGoodsPage) match {
      case None    => WhyIdenticalGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => nextPage(HaveYouUsedMethodOneInPastPage, userAnswers)
    }

  private def haveYouUsedMethodOneInPast(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneInPastPage) match {
      case None        => HaveYouUsedMethodOneInPastController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(true)  => nextPage(DescribeTheIdenticalGoodsPage, userAnswers)
      case Some(false) => nextPage(ValuationMethodPage, userAnswers)
    }

  private def describeTheIdenticalGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheIdenticalGoodsPage) match {
      case None    => DescribeTheIdenticalGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  // Method 3----------------------------------------------------------------
  private def whyTransactionValueOfSimilarGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyTransactionValueOfSimilarGoodsPage) match {
      case None    =>
        WhyTransactionValueOfSimilarGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) =>
        nextPage(HaveYouUsedMethodOneForSimilarGoodsInPastPage, userAnswers)
    }

  private def haveYouUsedMethodOneForSimilarGoodsInPast(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) match {
      case None        =>
        HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(
          CheckMode,
          userAnswers.draftId
        )
      case Some(true)  => nextPage(DescribeTheSimilarGoodsPage, userAnswers)
      case Some(false) => nextPage(ValuationMethodPage, userAnswers)
    }

  private def describeTheSimilarGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheSimilarGoodsPage) match {
      case None    => DescribeTheSimilarGoodsController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  // method 4 ----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToThree(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(
          CheckMode,
          userAnswers.draftId
        )
      case Some(_) => nextPage(ExplainWhyYouChoseMethodFourPage, userAnswers)
    }

  private def explainWhyYouChoseMethodFour(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouChoseMethodFourPage) match {
      case None    => ExplainWhyYouChoseMethodFourController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  // method 5----------------------------------------------------------------
  private def whyComputedValue(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyComputedValuePage) match {
      case None    => WhyComputedValueController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => nextPage(ExplainReasonComputedValuePage, userAnswers)
    }

  private def explainReasonComputedValue(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainReasonComputedValuePage) match {
      case None    => ExplainReasonComputedValueController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  // method 6----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToFivePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(
          CheckMode,
          userAnswers.draftId
        )
      case Some(_) => nextPage(AdaptMethodPage, userAnswers)
    }

  private def adaptMethodPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AdaptMethodPage) match {
      case None    => AdaptMethodController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => nextPage(ExplainHowYouWillUseMethodSixPage, userAnswers)
    }

  private def explainHowYouWillUseMethodSixPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainHowYouWillUseMethodSixPage) match {
      case None    =>
        ExplainHowYouWillUseMethodSixController.onPageLoad(CheckMode, userAnswers.draftId)
      case Some(_) => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }

  def nextPage(page: Page, userAnswers: UserAnswers): Call =
    page match {
      case CheckRegisteredDetailsPage => checkRegisteredDetails(userAnswers)

      case ValuationMethodPage                          => valuationMethod(userAnswers)
      case HasConfidentialInformationPage               => hasConfidentialInformation(userAnswers)
      case HaveTheGoodsBeenSubjectToLegalChallengesPage =>
        haveBeenSubjectToLegalChallenges(userAnswers)
      case HasCommodityCodePage                         => hasCommodityCode(userAnswers)
      case DoYouWantToUploadDocumentsPage               => doYouWantToUploadDocuments(userAnswers)
      case UploadSupportingDocumentPage                 => uploadSupportingDocumentPage(userAnswers)
      case IsThisFileConfidentialPage                   => isThisFileConfidential(userAnswers)
      case UploadAnotherSupportingDocumentPage          => uploadAnotherSupportingDocument(userAnswers)
      case WhatIsYourRoleAsImporterPage                 => whatIsYourRoleAsImporter(userAnswers)
      case RemoveSupportingDocumentPage(_)              => removeSupportingDocumentPage(userAnswers)

      // agent
      case UploadLetterOfAuthorityPage => uploadLetterOfAuthority(userAnswers)

      // method 1
      case IsThereASaleInvolvedPage           => isThereASaleInvolved(userAnswers)
      case IsSaleBetweenRelatedPartiesPage    => isSaleBetweenRelatedParties(userAnswers)
      case ExplainHowPartiesAreRelatedPage    => explainHowPartiesAreRelated(userAnswers)
      case AreThereRestrictionsOnTheGoodsPage => areThereRestrictionsOnTheGoods(userAnswers)
      case DescribeTheRestrictionsPage        => explainRestrictionsOnTheGoods(userAnswers)
      case IsTheSaleSubjectToConditionsPage   => isTheSaleSubjectToConditions(userAnswers)
      case DescribeTheConditionsPage          => explainTheConditions(userAnswers)

      // method 2
      case WhyIdenticalGoodsPage          => whyIdenticalGoods(userAnswers)
      case HaveYouUsedMethodOneInPastPage => haveYouUsedMethodOneInPast(userAnswers)
      case DescribeTheIdenticalGoodsPage  => describeTheIdenticalGoods(userAnswers)

      // method 3
      case WhyTransactionValueOfSimilarGoodsPage         => whyTransactionValueOfSimilarGoods(userAnswers)
      case HaveYouUsedMethodOneForSimilarGoodsInPastPage =>
        haveYouUsedMethodOneForSimilarGoodsInPast(userAnswers)
      case DescribeTheSimilarGoodsPage                   => describeTheSimilarGoods(userAnswers)

      // method 4
      case ExplainWhyYouHaveNotSelectedMethodOneToThreePage =>
        explainWhyYouHaveNotSelectedMethodOneToThree(userAnswers)
      case ExplainWhyYouChoseMethodFourPage                 => explainWhyYouChoseMethodFour(userAnswers)

      // method 5
      case WhyComputedValuePage           => whyComputedValue(userAnswers)
      case ExplainReasonComputedValuePage => explainReasonComputedValue(userAnswers)

      // method 6
      case ExplainWhyYouHaveNotSelectedMethodOneToFivePage =>
        explainWhyYouHaveNotSelectedMethodOneToFivePage(userAnswers)
      case ExplainHowYouWillUseMethodSixPage               => explainHowYouWillUseMethodSixPage(userAnswers)
      case AdaptMethodPage                                 => adaptMethodPage(userAnswers)
      case _                                               => CheckYourAnswersController.onPageLoad(userAnswers.draftId)

    }
}
