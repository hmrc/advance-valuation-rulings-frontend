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

import javax.inject.Inject

import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Individual

import controllers.routes._
import controllers.routes.UploadSupportingDocumentsController
import models._
import models.ValuationMethod._
import pages._

class Navigator @Inject() () {

  private def checkYourAnswers(): Call = CheckYourAnswersController.onPageLoad()

  private def checkYourAnswersForAgents(): Call = CheckYourAnswersForAgentsController.onPageLoad()

  private def routes(implicit affinityGroup: AffinityGroup): Page => UserAnswers => Call = {
    case ValuationMethodPage                           => valuationMethodPage
    case IsThereASaleInvolvedPage                      => isThereASaleInvolvedPage
    case IsSaleBetweenRelatedPartiesPage               => isSaleBetweenRelatedPartiesPage
    case ExplainHowPartiesAreRelatedPage               => explainHowPartiesAreRelatedPage
    case DescriptionOfGoodsPage                        => descriptionOfGoodsPage
    case HasCommodityCodePage                          => hasCommodityCodePage
    case CommodityCodePage                             => commodityCodePage
    case HaveTheGoodsBeenSubjectToLegalChallengesPage  =>
      haveTheGoodsBeenSubjectToLegalChallengesPage
    case DescribeTheLegalChallengesPage                => describeTheLegalChallengesPage
    case HasConfidentialInformationPage                => hasConfidentialInformationPage
    case ConfidentialInformationPage                   => confidentialInformationPage
    case ImportGoodsPage                               => importGoodsPage
    case RequiredInformationPage                       => requiredInformationPage
    case WhatIsYourRoleAsImporterPage                  => whatIsYourRoleAsImporterPage
    case CheckRegisteredDetailsPage                    => checkRegisteredDetailsPage
    case ApplicationContactDetailsPage                 => applicationContactDetailsPage
    case BusinessContactDetailsPage                    => businessContactDetailsPage
    case DoYouWantToUploadDocumentsPage                => doYouWantToUploadDocumentsPage
    case IsThisFileConfidentialPage                    => isThisFileConfidentialPage
    case UploadAnotherSupportingDocumentPage           => uploadAnotherSupportingDocumentPage
    case WhyComputedValuePage                          => whyComputedValuePage
    case ExplainReasonComputedValuePage                => explainReasonComputedValuePage
    case WhyTransactionValueOfSimilarGoodsPage         => whyTransactionValueOfSimilarGoodsPage
    case HaveYouUsedMethodOneForSimilarGoodsInPastPage =>
      haveYouUsedMethodOneForSimilarGoodsInPastPage
    case HaveYouUsedMethodOneInPastPage                => haveYouUsedMethodOneInPastPage
    case WhyIdenticalGoodsPage                         => whyIdenticalGoodsPage
    case AreThereRestrictionsOnTheGoodsPage            => areThereRestrictionsOnTheGoodsPage
    case DescribeTheRestrictionsPage                   => describeTheRestrictionsPage
    case IsTheSaleSubjectToConditionsPage              => isTheSaleSubjectToConditionsPage
    case DescribeTheConditionsPage                     => describeTheConditionsPage
    case DescribeTheIdenticalGoodsPage                 => describeTheIdenticalGoodsPage
    case ExplainYourGoodsComparingToIdenticalGoodsPage =>
      explainYourGoodsComparingToIdenticalGoodsPage
    case WillYouCompareGoodsToIdenticalGoodsPage       => willYouCompareGoodsToIdenticalGoodsPage

    case DescribeTheSimilarGoodsPage                 => describeTheSimilarGoodsPage
    case WillYouCompareToSimilarGoodsPage            => willYouCompareToSimilarGoodsPage
    case ExplainYourGoodsComparingToSimilarGoodsPage => explainYourGoodsComparingToSimilarGoodsPage

    case ExplainWhyYouHaveNotSelectedMethodOneToThreePage =>
      explainWhyYouHaveNotSelectedMethodOneToThreePage
    case ExplainWhyYouChoseMethodFourPage                 => explainWhyYouChoseMethodFourPage
    case ExplainWhyYouHaveNotSelectedMethodOneToFivePage  =>
      explainWhyYouHaveNotSelectedMethodOneToFivePage
    case ExplainHowYouWillUseMethodSixPage                => explainHowYouWillUseMethodSixPage
    case AdaptMethodPage                                  => adaptMethodPage
    case _                                                => _ => IndexController.onPageLoad
  }

  private def valuationMethodPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None                  => ValuationMethodController.onPageLoad(NormalMode)
      case Some(valuationMethod) =>
        import models.ValuationMethod._
        valuationMethod match {
          case Method1 => IsThereASaleInvolvedController.onPageLoad(NormalMode)
          case Method2 => WhyIdenticalGoodsController.onPageLoad(NormalMode)
          case Method3 => WhyTransactionValueOfSimilarGoodsController.onPageLoad(NormalMode)
          case Method4 =>
            ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(NormalMode)
          case Method5 => WhyComputedValueController.onPageLoad(NormalMode)
          case Method6 =>
            ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(NormalMode)
        }
    }
  // Method 1----------------------------------------------------------------

  private def areThereRestrictionsOnTheGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AreThereRestrictionsOnTheGoodsPage) match {
      case None        => AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode)
      case Some(true)  => DescribeTheRestrictionsController.onPageLoad(NormalMode)
      case Some(false) => IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode)
    }

  private def describeTheRestrictionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheRestrictionsPage) match {
      case None    => DescribeTheRestrictionsController.onPageLoad(NormalMode)
      case Some(_) => IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode)
    }

  private def isTheSaleSubjectToConditionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsTheSaleSubjectToConditionsPage) match {
      case None        => IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode)
      case Some(true)  => DescribeTheConditionsController.onPageLoad(NormalMode)
      case Some(false) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  private def describeTheConditionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheConditionsPage) match {
      case None    => DescribeTheConditionsController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  // Method 1----------------------------------------------------------------

  private def isThereASaleInvolvedPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsThereASaleInvolvedPage) match {
      case None        => IsThereASaleInvolvedController.onPageLoad(NormalMode)
      case Some(true)  => IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode)
      case Some(false) =>
        ValuationMethodController.onPageLoad(
          NormalMode
        )
    }

  private def isSaleBetweenRelatedPartiesPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsSaleBetweenRelatedPartiesPage) match {
      case None        => IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode)
      case Some(true)  => ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode)
      case Some(false) => AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode)
    }

  private def explainHowPartiesAreRelatedPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainHowPartiesAreRelatedPage) match {
      case None    => ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode)
      case Some(_) => AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode)
    }

  // Method 2----------------------------------------------------------------
  private def whyIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyIdenticalGoodsPage) match {
      case None    => WhyIdenticalGoodsController.onPageLoad(NormalMode)
      case Some(_) => HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode)
    }

  private def haveYouUsedMethodOneInPastPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneInPastPage) match {
      case None        => HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode)
      case Some(true)  =>
        DescribeTheIdenticalGoodsController.onPageLoad(NormalMode)
      case Some(false) =>
        WillYouCompareGoodsToIdenticalGoodsController.onPageLoad(NormalMode)
    }

  private def describeTheIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheIdenticalGoodsPage) match {
      case None    => DescribeTheIdenticalGoodsController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  private def willYouCompareGoodsToIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WillYouCompareGoodsToIdenticalGoodsPage) match {
      case None        => WillYouCompareGoodsToIdenticalGoodsController.onPageLoad(NormalMode)
      case Some(true)  =>
        ExplainYourGoodsComparingToIdenticalGoodsController.onPageLoad(NormalMode)
      case Some(false) => ValuationMethodController.onPageLoad(NormalMode)
    }

  private def explainYourGoodsComparingToIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainYourGoodsComparingToIdenticalGoodsPage) match {
      case None    => ExplainYourGoodsComparingToIdenticalGoodsController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  // Method 3----------------------------------------------------------------
  private def whyTransactionValueOfSimilarGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyTransactionValueOfSimilarGoodsPage) match {
      case None    => WhyTransactionValueOfSimilarGoodsController.onPageLoad(NormalMode)
      case Some(_) =>
        HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(NormalMode)
    }

  private def haveYouUsedMethodOneForSimilarGoodsInPastPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) match {
      case None        =>
        HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(NormalMode)
      case Some(true)  =>
        DescribeTheSimilarGoodsController.onPageLoad(NormalMode)
      case Some(false) =>
        WillYouCompareToSimilarGoodsController.onPageLoad(NormalMode)
    }

  private def describeTheSimilarGoodsPage(userAnswers: UserAnswers): Call                      =
    userAnswers.get(DescribeTheSimilarGoodsPage) match {
      case None    => DescribeTheSimilarGoodsController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }
  private def willYouCompareToSimilarGoodsPage(userAnswers: UserAnswers): Call                 =
    userAnswers.get(WillYouCompareToSimilarGoodsPage) match {
      case None        => WillYouCompareToSimilarGoodsController.onPageLoad(NormalMode)
      case Some(true)  =>
        ExplainYourGoodsComparingToSimilarGoodsController.onPageLoad(NormalMode)
      case Some(false) =>
        ValuationMethodController.onPageLoad(NormalMode)
    }
  private def explainYourGoodsComparingToSimilarGoodsPage(userAnswers: UserAnswers): Call      =
    userAnswers.get(ExplainYourGoodsComparingToSimilarGoodsPage) match {
      case None    => ExplainYourGoodsComparingToSimilarGoodsController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }
  // method 4 ----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToThreePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(NormalMode)
      case Some(_) => ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode)
    }

  private def explainWhyYouChoseMethodFourPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouChoseMethodFourPage) match {
      case None    => ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  // method 5----------------------------------------------------------------
  private def whyComputedValuePage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyComputedValuePage) match {
      case None    => WhyComputedValueController.onPageLoad(NormalMode)
      case Some(_) => ExplainReasonComputedValueController.onPageLoad(NormalMode)
    }

  private def explainReasonComputedValuePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainReasonComputedValuePage) match {
      case None    => ExplainReasonComputedValueController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  // method 6----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToFivePage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(NormalMode)
      case Some(_) => AdaptMethodController.onPageLoad(NormalMode)
    }

  private def adaptMethodPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(AdaptMethodPage) match {
      case None    => AdaptMethodController.onPageLoad(NormalMode)
      case Some(_) => ExplainHowYouWillUseMethodSixController.onPageLoad(NormalMode)
    }

  private def explainHowYouWillUseMethodSixPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ExplainHowYouWillUseMethodSixPage) match {
      case None    => ExplainHowYouWillUseMethodSixController.onPageLoad(NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode)
    }

  // ----------------------------------------------------------------
  private def descriptionOfGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescriptionOfGoodsPage) match {
      case None    => DescriptionOfGoodsController.onPageLoad(NormalMode)
      case Some(_) => HasCommodityCodeController.onPageLoad(NormalMode)
    }

  private def hasCommodityCodePage(userAnswers: UserAnswers): Call                         =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(NormalMode)
      case Some(true)  => CommodityCodeController.onPageLoad(NormalMode)
      case Some(false) =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode)
    }
  private def haveTheGoodsBeenSubjectToLegalChallengesPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage) match {
      case None        => HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode)
      case Some(true)  => DescribeTheLegalChallengesController.onPageLoad(NormalMode)
      case Some(false) =>
        HasConfidentialInformationController.onPageLoad(NormalMode)
    }
  private def describeTheLegalChallengesPage(userAnswers: UserAnswers): Call               =
    userAnswers.get(DescribeTheLegalChallengesPage) match {
      case None    => DescribeTheLegalChallengesController.onPageLoad(NormalMode)
      case Some(_) => HasConfidentialInformationController.onPageLoad(NormalMode)

    }

  private def commodityCodePage(userAnswers: UserAnswers): Call =
    userAnswers.get(CommodityCodePage) match {
      case None    => CommodityCodeController.onPageLoad(NormalMode)
      case Some(_) =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(NormalMode)
    }

  private def hasConfidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HasConfidentialInformationPage) match {
      case None        => HasConfidentialInformationController.onPageLoad(NormalMode)
      case Some(true)  => ConfidentialInformationController.onPageLoad(NormalMode)
      case Some(false) => DoYouWantToUploadDocumentsController.onPageLoad(NormalMode)
    }

  private def confidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ConfidentialInformationPage) match {
      case None    => ConfidentialInformationController.onPageLoad(NormalMode)
      case Some(_) => DoYouWantToUploadDocumentsController.onPageLoad(NormalMode)
    }

  private def doYouWantToUploadDocumentsPage(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(NormalMode)
      case Some(true)  =>
        UploadSupportingDocumentsController
          .onPageLoad(None, None, None, NormalMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers(), checkYourAnswersForAgents())
    }

  private def isThisFileConfidentialPage(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    userAnswers.get(UploadSupportingDocumentPage) match {
      case None                => doYouWantToUploadDocumentsPage(userAnswers)
      case Some(uploadedFiles) =>
        uploadedFiles match {
          case UploadedFiles(Some(_), _)                    =>
            IsThisFileConfidentialController.onPageLoad(NormalMode)
          case UploadedFiles(None, files) if files.nonEmpty =>
            UploadAnotherSupportingDocumentController.onPageLoad(NormalMode)
          case UploadedFiles(None, _)                       =>
            DoYouWantToUploadDocumentsController.onPageLoad(NormalMode)
        }
    }

  private def uploadAnotherSupportingDocumentPage(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    userAnswers.get(UploadAnotherSupportingDocumentPage) match {
      case None        => UploadAnotherSupportingDocumentController.onPageLoad(NormalMode)
      case Some(true)  =>
        UploadSupportingDocumentsController
          .onPageLoad(None, None, None, NormalMode)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(checkYourAnswers(), checkYourAnswersForAgents())
    }

  private def importGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ImportGoodsPage) match {
      case None        => ImportGoodsController.onPageLoad(NormalMode)
      case Some(true)  => ContactPageController.onPageLoad()
      case Some(false) => ImportingGoodsController.onPageLoad()
    }

  private def requiredInformationPage(userAnswers: UserAnswers): Call      =
    userAnswers.get(RequiredInformationPage) match {
      case None    => RequiredInformationController.onPageLoad()
      case Some(_) => ImportGoodsController.onPageLoad(NormalMode)
    }
  private def whatIsYourRoleAsImporterPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None    => WhatIsYourRoleAsImporterController.onPageLoad(NormalMode)
      case Some(_) => RequiredInformationController.onPageLoad()
    }
  private def checkRegisteredDetailsPage(userAnswers: UserAnswers): Call   =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None                    => CheckRegisteredDetailsController.onPageLoad(NormalMode)
      case Some(registeredDetails) =>
        if (registeredDetails.value)
          ApplicationContactDetailsController.onPageLoad(NormalMode)
        else EORIBeUpToDateController.onPageLoad()
    }

  private def applicationContactDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ApplicationContactDetailsPage) match {
      case None    => ApplicationContactDetailsController.onPageLoad(NormalMode)
      case Some(_) => ValuationMethodController.onPageLoad(NormalMode)
    }

  private def businessContactDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(BusinessContactDetailsPage) match {
      case None    => BusinessContactDetailsController.onPageLoad(NormalMode)
      case Some(_) => ValuationMethodController.onPageLoad(NormalMode)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit
    affinityGroup: AffinityGroup
  ): Call = mode match {
    case NormalMode =>
      routes(affinityGroup)(page)(userAnswers)
    case CheckMode  =>
      CheckModeNavigator.nextPage(page)(userAnswers, affinityGroup)
  }

  def startApplicationRouting(affinityGroup: AffinityGroup): Call =
    affinityGroup match {
      case Individual => RequiredInformationController.onPageLoad()
      case _          =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode)
    }

  def contactDetailsRouting(affinityGroup: AffinityGroup, userAnswers: UserAnswers): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None                    => CheckRegisteredDetailsController.onPageLoad(NormalMode)
      case Some(registeredDetails) =>
        if (registeredDetails.value) {
          affinityGroup match {
            case Individual => ApplicationContactDetailsController.onPageLoad(NormalMode)
            case _          => BusinessContactDetailsController.onPageLoad(NormalMode)
          }
        } else EORIBeUpToDateController.onPageLoad()
    }
}
