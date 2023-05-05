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

import controllers.routes._
import models._
import models.AuthUserType.{IndividualTrader, OrganisationAdmin, OrganisationAssistant}
import models.ValuationMethod._
import pages._
import queries.AllDocuments

class Navigator @Inject() () {

  private def checkYourAnswers(draftId: DraftId): Call =
    CheckYourAnswersController.onPageLoad(draftId)

  private def checkYourAnswersForAgents(draftId: DraftId): Call =
    CheckYourAnswersForAgentsController.onPageLoad(draftId)

  private def routes(implicit affinityGroup: AffinityGroup): Page => UserAnswers => Call = {
    case ValuationMethodPage                              => valuationMethodPage
    case IsThereASaleInvolvedPage                         => isThereASaleInvolvedPage
    case IsSaleBetweenRelatedPartiesPage                  => isSaleBetweenRelatedPartiesPage
    case ExplainHowPartiesAreRelatedPage                  => explainHowPartiesAreRelatedPage
    case DescriptionOfGoodsPage                           => descriptionOfGoodsPage
    case HasCommodityCodePage                             => hasCommodityCodePage
    case CommodityCodePage                                => commodityCodePage
    case HaveTheGoodsBeenSubjectToLegalChallengesPage     =>
      haveTheGoodsBeenSubjectToLegalChallengesPage
    case DescribeTheLegalChallengesPage                   => describeTheLegalChallengesPage
    case HasConfidentialInformationPage                   => hasConfidentialInformationPage
    case ConfidentialInformationPage                      => confidentialInformationPage
    case ImportGoodsPage                                  => importGoodsPage
    case RequiredInformationPage                          => requiredInformationPage
    case WhatIsYourRoleAsImporterPage                     => whatIsYourRoleAsImporterPage
    case ContactPagePage                                  =>
      ua => CheckRegisteredDetailsController.onPageLoad(NormalMode, ua.draftId)
    case CheckRegisteredDetailsPage                       => checkRegisteredDetailsPage
    case ApplicationContactDetailsPage                    => applicationContactDetailsPage
    case BusinessContactDetailsPage                       => businessContactDetailsPage
    case AgentCompanyDetailsPage                          => agentCompanyDetailsPage
    case DoYouWantToUploadDocumentsPage                   => doYouWantToUploadDocumentsPage
    case UploadSupportingDocumentPage(index)              => uploadSupportingDocumentPage(index)
    case IsThisFileConfidentialPage(index)                => isThisFileConfidentialPage(index)
    case UploadAnotherSupportingDocumentPage              => uploadAnotherSupportingDocumentPage
    case DeleteSupportingDocumentPage(_)                  => deleteSupportingDocumentPage
    case WhyComputedValuePage                             => whyComputedValuePage
    case ExplainReasonComputedValuePage                   => explainReasonComputedValuePage
    case WhyTransactionValueOfSimilarGoodsPage            => whyTransactionValueOfSimilarGoodsPage
    case HaveYouUsedMethodOneForSimilarGoodsInPastPage    =>
      haveYouUsedMethodOneForSimilarGoodsInPastPage
    case HaveYouUsedMethodOneInPastPage                   => haveYouUsedMethodOneInPastPage
    case WhyIdenticalGoodsPage                            => whyIdenticalGoodsPage
    case AreThereRestrictionsOnTheGoodsPage               => areThereRestrictionsOnTheGoodsPage
    case DescribeTheRestrictionsPage                      => describeTheRestrictionsPage
    case IsTheSaleSubjectToConditionsPage                 => isTheSaleSubjectToConditionsPage
    case DescribeTheConditionsPage                        => describeTheConditionsPage
    case DescribeTheIdenticalGoodsPage                    => describeTheIdenticalGoodsPage
    case DescribeTheSimilarGoodsPage                      => describeTheSimilarGoodsPage
    case ExplainWhyYouHaveNotSelectedMethodOneToThreePage =>
      explainWhyYouHaveNotSelectedMethodOneToThreePage
    case ExplainWhyYouChoseMethodFourPage                 => explainWhyYouChoseMethodFourPage
    case ExplainWhyYouHaveNotSelectedMethodOneToFivePage  =>
      explainWhyYouHaveNotSelectedMethodOneToFivePage
    case ExplainHowYouWillUseMethodSixPage                => explainHowYouWillUseMethodSixPage
    case AdaptMethodPage                                  => adaptMethodPage
    case DeleteDraftPage                                  => _ => AccountHomeController.onPageLoad()
    case _                                                => _ => AccountHomeController.onPageLoad()
  }

  private def valuationMethodPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None                  => ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(valuationMethod) =>
        import models.ValuationMethod._
        valuationMethod match {
          case Method1 => IsThereASaleInvolvedController.onPageLoad(NormalMode, userAnswers.draftId)
          case Method2 => WhyIdenticalGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
          case Method3 =>
            WhyTransactionValueOfSimilarGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
          case Method4 =>
            ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(
              NormalMode,
              userAnswers.draftId
            )
          case Method5 => WhyComputedValueController.onPageLoad(NormalMode, userAnswers.draftId)
          case Method6 =>
            ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(
              NormalMode,
              userAnswers.draftId
            )
        }
    }
  // Method 1----------------------------------------------------------------

  private def areThereRestrictionsOnTheGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AreThereRestrictionsOnTheGoodsPage) match {
      case None        =>
        AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        DescribeTheRestrictionsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def describeTheRestrictionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheRestrictionsPage) match {
      case None    => DescribeTheRestrictionsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def isTheSaleSubjectToConditionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsTheSaleSubjectToConditionsPage) match {
      case None        =>
        IsTheSaleSubjectToConditionsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  => DescribeTheConditionsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def describeTheConditionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheConditionsPage) match {
      case None    => DescribeTheConditionsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // Method 1----------------------------------------------------------------

  private def isThereASaleInvolvedPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsThereASaleInvolvedPage) match {
      case None        => IsThereASaleInvolvedController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        ValuationMethodController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
    }

  private def isSaleBetweenRelatedPartiesPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsSaleBetweenRelatedPartiesPage) match {
      case None        => IsSaleBetweenRelatedPartiesController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def explainHowPartiesAreRelatedPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainHowPartiesAreRelatedPage) match {
      case None    => ExplainHowPartiesAreRelatedController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        AreThereRestrictionsOnTheGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // Method 2----------------------------------------------------------------
  private def whyIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyIdenticalGoodsPage) match {
      case None    => WhyIdenticalGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def haveYouUsedMethodOneInPastPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneInPastPage) match {
      case None        => HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        DescribeTheIdenticalGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def describeTheIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheIdenticalGoodsPage) match {
      case None    => DescribeTheIdenticalGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // Method 3----------------------------------------------------------------
  private def whyTransactionValueOfSimilarGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyTransactionValueOfSimilarGoodsPage) match {
      case None    =>
        WhyTransactionValueOfSimilarGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
    }

  private def haveYouUsedMethodOneForSimilarGoodsInPastPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) match {
      case None        =>
        HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
      case Some(true)  =>
        DescribeTheSimilarGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def describeTheSimilarGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheSimilarGoodsPage) match {
      case None    => DescribeTheSimilarGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // method 4 ----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToThreePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
      case Some(_) =>
        ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def explainWhyYouChoseMethodFourPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouChoseMethodFourPage) match {
      case None    =>
        ExplainWhyYouChoseMethodFourController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // method 5----------------------------------------------------------------
  private def whyComputedValuePage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyComputedValuePage) match {
      case None    => WhyComputedValueController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        ExplainReasonComputedValueController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def explainReasonComputedValuePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainReasonComputedValuePage) match {
      case None    => ExplainReasonComputedValueController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // method 6----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToFivePage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
      case Some(_) => AdaptMethodController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def adaptMethodPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(AdaptMethodPage) match {
      case None    => AdaptMethodController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        ExplainHowYouWillUseMethodSixController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def explainHowYouWillUseMethodSixPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ExplainHowYouWillUseMethodSixPage) match {
      case None    =>
        ExplainHowYouWillUseMethodSixController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  // ----------------------------------------------------------------
  private def descriptionOfGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescriptionOfGoodsPage) match {
      case None    => DescriptionOfGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => HasCommodityCodeController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def hasCommodityCodePage(userAnswers: UserAnswers): Call                         =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  => CommodityCodeController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
    }
  private def haveTheGoodsBeenSubjectToLegalChallengesPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage) match {
      case None        =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
      case Some(true)  =>
        DescribeTheLegalChallengesController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        HasConfidentialInformationController.onPageLoad(NormalMode, userAnswers.draftId)
    }
  private def describeTheLegalChallengesPage(userAnswers: UserAnswers): Call               =
    userAnswers.get(DescribeTheLegalChallengesPage) match {
      case None    => DescribeTheLegalChallengesController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        HasConfidentialInformationController.onPageLoad(NormalMode, userAnswers.draftId)

    }

  private def commodityCodePage(userAnswers: UserAnswers): Call =
    userAnswers.get(CommodityCodePage) match {
      case None    => CommodityCodeController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(
          NormalMode,
          userAnswers.draftId
        )
    }

  private def hasConfidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HasConfidentialInformationPage) match {
      case None        => HasConfidentialInformationController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        ConfidentialInformationController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) =>
        val numberOfDocuments = userAnswers.get(AllDocuments).getOrElse(Seq.empty).size
        if (numberOfDocuments > 0) {
          controllers.routes.UploadAnotherSupportingDocumentController
            .onPageLoad(NormalMode, userAnswers.draftId)
        } else {
          DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
        }
    }

  private def confidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ConfidentialInformationPage) match {
      case None    => ConfidentialInformationController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        val numberOfDocuments = userAnswers.get(AllDocuments).getOrElse(Seq.empty).size
        if (numberOfDocuments > 0) {
          controllers.routes.UploadAnotherSupportingDocumentController
            .onPageLoad(NormalMode, userAnswers.draftId)
        } else {
          DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
        }
    }

  private def doYouWantToUploadDocumentsPage(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        UploadSupportingDocumentsController
          .onPageLoad(Index(0), NormalMode, userAnswers.draftId, None, None)
      case Some(false) =>
        resolveAffinityGroup(affinityGroup)(
          checkYourAnswers(userAnswers.draftId),
          checkYourAnswersForAgents(userAnswers.draftId)
        )
    }

  private def uploadSupportingDocumentPage(index: Index)(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    controllers.routes.IsThisFileConfidentialController.onPageLoad(
      index,
      NormalMode,
      userAnswers.draftId
    )

  private def isThisFileConfidentialPage(index: Index)(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    controllers.routes.UploadAnotherSupportingDocumentController
      .onPageLoad(NormalMode, userAnswers.draftId)

  private def uploadAnotherSupportingDocumentPage(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    userAnswers
      .get(UploadAnotherSupportingDocumentPage)
      .map {
        case true  =>
          val nextIndex = userAnswers.get(AllDocuments).map(_.size).getOrElse(0)
          controllers.routes.UploadSupportingDocumentsController.onPageLoad(
            Index(nextIndex),
            NormalMode,
            userAnswers.draftId,
            None,
            None
          )
        case false =>
          resolveAffinityGroup(affinityGroup)(
            checkYourAnswers(userAnswers.draftId),
            checkYourAnswersForAgents(userAnswers.draftId)
          )
      }
      .getOrElse(controllers.routes.JourneyRecoveryController.onPageLoad())

  private def deleteSupportingDocumentPage(
    userAnswers: UserAnswers
  ): Call = {
    val numberOfDocuments = userAnswers.get(AllDocuments).map(_.size).getOrElse(0)
    if (numberOfDocuments > 0) {
      controllers.routes.UploadAnotherSupportingDocumentController
        .onPageLoad(NormalMode, userAnswers.draftId)
    } else {
      controllers.routes.DoYouWantToUploadDocumentsController
        .onPageLoad(NormalMode, userAnswers.draftId)
    }
  }

  private def importGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ImportGoodsPage) match {
      case None        => ImportGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  => ContactPageController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) => ImportingGoodsController.onPageLoad(userAnswers.draftId)
    }

  private def requiredInformationPage(userAnswers: UserAnswers): Call      =
    userAnswers.get(RequiredInformationPage) match {
      case None    => RequiredInformationController.onPageLoad(userAnswers.draftId)
      case Some(_) => ImportGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
    }
  private def whatIsYourRoleAsImporterPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None    => WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => RequiredInformationController.onPageLoad(userAnswers.draftId)
    }
  private def checkRegisteredDetailsPage(
    userAnswers: UserAnswers
  )(implicit affinityGroup: AffinityGroup): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None        => CheckRegisteredDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(value) =>
        if (value) {
          resolveAffinityGroup(affinityGroup)(
            ApplicationContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId),
            BusinessContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
          )
        } else EORIBeUpToDateController.onPageLoad(userAnswers.draftId)
    }

  private def applicationContactDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ApplicationContactDetailsPage) match {
      case None    => ApplicationContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def businessContactDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(BusinessContactDetailsPage) match {
      case None    => BusinessContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => agentContactDetailsNavigation(userAnswers)
    }

  private def agentContactDetailsNavigation(userAnswers: UserAnswers): Call =
    userAnswers.get(ApplicantUserType) match {
      case Some(OrganisationAdmin)     =>
        ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(OrganisationAssistant) =>
        AgentCompanyDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case _                           =>
        UnauthorisedController.onPageLoad
    }

  private def agentCompanyDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AgentCompanyDetailsPage) match {
      case None    => AgentCompanyDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers)(implicit
    affinityGroup: AffinityGroup
  ): Call = mode match {
    case NormalMode =>
      routes(affinityGroup)(page)(userAnswers)
    case CheckMode  =>
      CheckModeNavigator.nextPage(page)(userAnswers, affinityGroup)
  }

  def startApplicationRouting(userAnswers: UserAnswers): Call =
    userAnswers.get(ApplicantUserType) match {
      case Some(IndividualTrader)      =>
        RequiredInformationController.onPageLoad(userAnswers.draftId)
      case Some(OrganisationAdmin)     =>
        RequiredInformationController.onPageLoad(userAnswers.draftId)
      case Some(OrganisationAssistant) =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case _                           =>
        UnauthorisedController.onPageLoad
    }
}
