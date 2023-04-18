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
import uk.gov.hmrc.auth.core.AffinityGroup

import controllers.routes._
import controllers.routes.UploadSupportingDocumentsController
import models._
import models.CheckMode
import models.ValuationMethod._
import pages._

object CheckModeNavigator {
  import controllers._

  private def checkYourAnswers = routes.CheckYourAnswersController.onPageLoad

  private def checkYourAnswersForAgents = routes.CheckYourAnswersForAgentsController.onPageLoad

  // Pre nav
  private def checkRegisteredDetails(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None                    => CheckRegisteredDetailsController.onPageLoad(CheckMode)
      case Some(registeredDetails) =>
        if (registeredDetails.value) {
          resolveAffinityGroup(affinityGroup)(
            checkYourAnswers,
            checkYourAnswersForAgents
          )
        } else {
          EORIBeUpToDateController.onPageLoad()
        }
    }

  // Post navigation
  private def hasConfidentialInformation(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(HasConfidentialInformationPage) match {
      case None        => HasConfidentialInformationController.onPageLoad(CheckMode)
      case Some(true)  => ConfidentialInformationController.onPageLoad(CheckMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  private def haveBeenSubjectToLegalChallenges(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage) match {
      case None        => HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(CheckMode)
      case Some(true)  => DescribeTheLegalChallengesController.onPageLoad(CheckMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  private def hasCommodityCode(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(CheckMode)
      case Some(true)  => CommodityCodeController.onPageLoad(CheckMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  private def whatIsYourRoleAsImporter(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None                                              => WhatIsYourRoleAsImporterController.onPageLoad(CheckMode)
      case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg) =>
        AgentCompanyDetailsController.onPageLoad(CheckMode)
      case Some(WhatIsYourRoleAsImporter.EmployeeOfOrg)      =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  private def doYouWantToUploadDocuments(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(CheckMode)
      case Some(true)  =>
        controllers.routes.UploadSupportingDocumentsController
          .onPageLoad(None, None, None, CheckMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  private def isThisFileConfidential(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(UploadSupportingDocumentPage) match {
      case None                => doYouWantToUploadDocuments(userAnswers, affinityGroup)
      case Some(uploadedFiles) =>
        uploadedFiles match {
          case UploadedFiles(Some(_), _)                    =>
            IsThisFileConfidentialController.onPageLoad(CheckMode)
          case UploadedFiles(None, files) if files.nonEmpty =>
            UploadAnotherSupportingDocumentController.onPageLoad(CheckMode)
          case UploadedFiles(None, _)                       =>
            DoYouWantToUploadDocumentsController.onPageLoad(CheckMode)
        }
    }

  private def uploadAnotherSupportingDocument(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(UploadAnotherSupportingDocumentPage) match {
      case None        => UploadAnotherSupportingDocumentController.onPageLoad(CheckMode)
      case Some(true)  =>
        UploadSupportingDocumentsController
          .onPageLoad(None, None, None, CheckMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  // Valuation Method
  private def valuationMethod(implicit userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None          => ValuationMethodController.onPageLoad(CheckMode)
      case Some(Method1) => IsThereASaleInvolvedController.onPageLoad(CheckMode)
      case Some(Method2) => WhyIdenticalGoodsController.onPageLoad(CheckMode)
      case Some(Method3) =>
        WhyTransactionValueOfSimilarGoodsController.onPageLoad(CheckMode)
      case Some(Method4) =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(CheckMode)
      case Some(Method5) => WhyComputedValueController.onPageLoad(CheckMode)
      case Some(Method6) =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(CheckMode)
    }

  // Method 1----------------------------------------------------------------
  private def isThereASaleInvolved(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(IsThereASaleInvolvedPage) match {
      case None        => IsThereASaleInvolvedController.onPageLoad(CheckMode)
      case Some(true)  => nextPage(IsSaleBetweenRelatedPartiesPage)
      case Some(false) =>
        ValuationMethodController.onPageLoad(
          CheckMode
        )
    }

  private def isSaleBetweenRelatedParties(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(IsSaleBetweenRelatedPartiesPage) match {
      case None        => IsSaleBetweenRelatedPartiesController.onPageLoad(CheckMode)
      case Some(true)  => nextPage(ExplainHowPartiesAreRelatedPage)
      case Some(false) => nextPage(AreThereRestrictionsOnTheGoodsPage)
    }

  private def explainHowPartiesAreRelated(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(ExplainHowPartiesAreRelatedPage) match {
      case None    => ExplainHowPartiesAreRelatedController.onPageLoad(CheckMode)
      case Some(_) => nextPage(AreThereRestrictionsOnTheGoodsPage)
    }

  private def areThereRestrictionsOnTheGoods(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(AreThereRestrictionsOnTheGoodsPage) match {
      case None        => AreThereRestrictionsOnTheGoodsController.onPageLoad(CheckMode)
      case Some(true)  => nextPage(DescribeTheRestrictionsPage)
      case Some(false) => nextPage(IsTheSaleSubjectToConditionsPage)
    }

  private def explainRestrictionsOnTheGoods(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(DescribeTheRestrictionsPage) match {
      case None    => DescribeTheRestrictionsController.onPageLoad(CheckMode)
      case Some(_) => nextPage(IsTheSaleSubjectToConditionsPage)
    }

  private def isTheSaleSubjectToConditions(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(IsTheSaleSubjectToConditionsPage) match {
      case None        => IsTheSaleSubjectToConditionsController.onPageLoad(CheckMode)
      case Some(true)  => nextPage(DescribeTheConditionsPage)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  private def explainTheConditions(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(DescribeTheConditionsPage) match {
      case None    => DescribeTheConditionsController.onPageLoad(CheckMode)
      case Some(_) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  // Method 2----------------------------------------------------------------
  private def whyIdenticalGoods(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(WhyIdenticalGoodsPage) match {
      case None    => WhyIdenticalGoodsController.onPageLoad(CheckMode)
      case Some(_) => nextPage(HaveYouUsedMethodOneInPastPage)
    }

  private def haveYouUsedMethodOneInPast(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(HaveYouUsedMethodOneInPastPage) match {
      case None        => HaveYouUsedMethodOneInPastController.onPageLoad(CheckMode)
      case Some(true)  => nextPage(DescribeTheIdenticalGoodsPage)
      case Some(false) => nextPage(ValuationMethodPage)
    }

  private def describeTheIdenticalGoods(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(DescribeTheIdenticalGoodsPage) match {
      case None    => DescribeTheIdenticalGoodsController.onPageLoad(CheckMode)
      case Some(_) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  // Method 3----------------------------------------------------------------
  private def whyTransactionValueOfSimilarGoods(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(WhyTransactionValueOfSimilarGoodsPage) match {
      case None    => WhyTransactionValueOfSimilarGoodsController.onPageLoad(CheckMode)
      case Some(_) =>
        nextPage(HaveYouUsedMethodOneForSimilarGoodsInPastPage)
    }

  private def haveYouUsedMethodOneForSimilarGoodsInPast(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) match {
      case None        =>
        HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(CheckMode)
      case Some(true)  => nextPage(DescribeTheSimilarGoodsPage)
      case Some(false) => nextPage(ValuationMethodPage)
    }

  private def describeTheSimilarGoods(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(DescribeTheSimilarGoodsPage) match {
      case None    => DescribeTheSimilarGoodsController.onPageLoad(CheckMode)
      case Some(_) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  // method 4 ----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToThree(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(CheckMode)
      case Some(_) => nextPage(ExplainWhyYouChoseMethodFourPage)
    }

  private def explainWhyYouChoseMethodFour(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(ExplainWhyYouChoseMethodFourPage) match {
      case None    => ExplainWhyYouChoseMethodFourController.onPageLoad(CheckMode)
      case Some(_) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  // method 5----------------------------------------------------------------
  private def whyComputedValue(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(WhyComputedValuePage) match {
      case None    => WhyComputedValueController.onPageLoad(CheckMode)
      case Some(_) => nextPage(ExplainReasonComputedValuePage)
    }

  private def explainReasonComputedValue(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(ExplainReasonComputedValuePage) match {
      case None    => ExplainReasonComputedValueController.onPageLoad(CheckMode)
      case Some(_) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  // method 6----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToFivePage(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(CheckMode)
      case Some(_) => nextPage(AdaptMethodPage)
    }

  private def adaptMethodPage(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(AdaptMethodPage) match {
      case None    => AdaptMethodController.onPageLoad(CheckMode)
      case Some(_) => nextPage(ExplainHowYouWillUseMethodSixPage)
    }

  private def explainHowYouWillUseMethodSixPage(implicit
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Call =
    userAnswers.get(ExplainHowYouWillUseMethodSixPage) match {
      case None    => ExplainHowYouWillUseMethodSixController.onPageLoad(CheckMode)
      case Some(_) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }

  def nextPage(page: Page)(implicit userAnswers: UserAnswers, affinityGroup: AffinityGroup): Call =
    page match {
      case CheckRegisteredDetailsPage => checkRegisteredDetails

      case ValuationMethodPage                          => valuationMethod
      case HasConfidentialInformationPage               => hasConfidentialInformation
      case HaveTheGoodsBeenSubjectToLegalChallengesPage => haveBeenSubjectToLegalChallenges
      case HasCommodityCodePage                         => hasCommodityCode
      case DoYouWantToUploadDocumentsPage               => doYouWantToUploadDocuments
      case IsThisFileConfidentialPage                   => isThisFileConfidential
      case UploadAnotherSupportingDocumentPage          => uploadAnotherSupportingDocument

      case WhatIsYourRoleAsImporterPage => whatIsYourRoleAsImporter

      // method 1
      case IsThereASaleInvolvedPage           => isThereASaleInvolved
      case IsSaleBetweenRelatedPartiesPage    => isSaleBetweenRelatedParties
      case ExplainHowPartiesAreRelatedPage    => explainHowPartiesAreRelated
      case AreThereRestrictionsOnTheGoodsPage => areThereRestrictionsOnTheGoods
      case DescribeTheRestrictionsPage        => explainRestrictionsOnTheGoods
      case IsTheSaleSubjectToConditionsPage   => isTheSaleSubjectToConditions
      case DescribeTheConditionsPage          => explainTheConditions

      // method 2
      case WhyIdenticalGoodsPage          => whyIdenticalGoods
      case HaveYouUsedMethodOneInPastPage => haveYouUsedMethodOneInPast
      case DescribeTheIdenticalGoodsPage  => describeTheIdenticalGoods

      // method 3
      case WhyTransactionValueOfSimilarGoodsPage         => whyTransactionValueOfSimilarGoods
      case HaveYouUsedMethodOneForSimilarGoodsInPastPage =>
        haveYouUsedMethodOneForSimilarGoodsInPast
      case DescribeTheSimilarGoodsPage                   => describeTheSimilarGoods

      // method 4
      case ExplainWhyYouHaveNotSelectedMethodOneToThreePage =>
        explainWhyYouHaveNotSelectedMethodOneToThree
      case ExplainWhyYouChoseMethodFourPage                 => explainWhyYouChoseMethodFour

      // method 5
      case WhyComputedValuePage           => whyComputedValue
      case ExplainReasonComputedValuePage => explainReasonComputedValue

      // method 6
      case ExplainWhyYouHaveNotSelectedMethodOneToFivePage =>
        explainWhyYouHaveNotSelectedMethodOneToFivePage
      case ExplainHowYouWillUseMethodSixPage               => explainHowYouWillUseMethodSixPage
      case AdaptMethodPage                                 => adaptMethodPage
      case _                                               =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers, checkYourAnswersForAgents)
    }
}
