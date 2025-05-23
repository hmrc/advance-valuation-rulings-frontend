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

package navigation

import controllers.routes._
import models.ValuationMethod._
import models._
import pages._
import play.api.mvc.Call
import queries.AllDocuments
import userrole.UserRoleProvider

import javax.inject.Inject

class Navigator @Inject() (userRoleProvider: UserRoleProvider, unchangedModeNavigator: UnchangedModeNavigator) {

  private def routes: Page => UserAnswers => Call = {
    case AccountHomePage                                  => startApplicationRouting
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
    case CancelApplicationPage                            => cancelApplicationPage
    case WhatIsYourRoleAsImporterPage                     => whatIsYourRoleAsImporterPage
    case ChangeYourRoleImporterPage                       => changeYourRoleImporterPageRoute
    case TellUsAboutYourRulingPage                        => tellUsAboutYourRuling
    case HaveYouReceivedADecisionPage                     => haveYouReceivedADecision
    case AwareOfRulingPage                                => awareOfRulingsWithSimilarMethod
    case AboutSimilarGoodsPage                            => aboutSimilarGoods
    case ContactPagePage                                  => contactsNextPage
    case CheckRegisteredDetailsPage                       => checkRegisteredDetailsPage
    case ApplicationContactDetailsPage                    => applicationContactDetailsPage
    case AgentCompanyDetailsPage                          => agentCompanyDetailsPage
    case DoYouWantToUploadDocumentsPage                   => doYouWantToUploadDocumentsPage
    case UploadSupportingDocumentPage                     => uploadSupportingDocumentPage
    case IsThisFileConfidentialPage                       => isThisFileConfidentialPage
    case UploadAnotherSupportingDocumentPage              => uploadAnotherSupportingDocumentPage
    case RemoveSupportingDocumentPage(_)                  => removeSupportingDocumentPage
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
    case AgentForTraderCheckRegisteredDetailsPage         =>
      ua =>
        UploadLetterOfAuthorityController.onPageLoad(
          NormalMode,
          ua.draftId,
          None,
          None,
          redirectedFromChangeButton = false
        )
    case AgentForOrgCheckRegisteredDetailsPage            =>
      ua => BusinessContactDetailsController.onPageLoad(NormalMode, ua.draftId)
    case AgentForOrgApplicationContactDetailsPage         =>
      ua => AgentCompanyDetailsController.onPageLoad(NormalMode, ua.draftId)
    case UploadLetterOfAuthorityPage                      => uploadLetterOfAuthorityPage
    case VerifyLetterOfAuthorityPage                      => verifyLetterOfAuthorityPage
    case EORIBeUpToDatePage                               => ua => EORIBeUpToDateController.onPageLoad(ua.draftId)
    case ProvideTraderEoriPage                            =>
      ua => VerifyTraderEoriController.onPageLoad(NormalMode, ua.draftId)
    case ChoosingMethodPage                               => ua => ValuationMethodController.onPageLoad(NormalMode, ua.draftId)
    case AgentForTraderContactDetailsPage                 => ua => ChoosingMethodController.onPageLoad(ua.draftId)
    case _                                                => _ => AccountHomeController.onPageLoad()
  }

  private def startApplicationRouting(userAnswers: UserAnswers): Call =
    userAnswers.get(AccountHomePage) match {
      case Some(_) =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case _       => UnauthorisedController.onPageLoad
    }

  private def valuationMethodPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None                  => ChoosingMethodController.onPageLoad(userAnswers.draftId)
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
      case Some(_) => HaveYouReceivedADecisionController.onPageLoad(NormalMode, userAnswers.draftId)
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
        val documents = userAnswers.getOrElse(AllDocuments, List.empty)
        if (documents.isEmpty) {
          DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
        } else {
          UploadAnotherSupportingDocumentController
            .onPageLoad(NormalMode, userAnswers.draftId)
        }
    }

  private def confidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ConfidentialInformationPage) match {
      case None    => ConfidentialInformationController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) =>
        val documents = userAnswers.getOrElse(AllDocuments, List.empty)
        if (documents.isEmpty) {
          DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
        } else {
          UploadAnotherSupportingDocumentController
            .onPageLoad(NormalMode, userAnswers.draftId)
        }
    }

  private def doYouWantToUploadDocumentsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  =>
        UploadSupportingDocumentsController
          .onPageLoad(NormalMode, userAnswers.draftId, None, None)
      case Some(false) =>
        CheckYourAnswersController.onPageLoad(userAnswers.draftId)
    }

  private def uploadSupportingDocumentPage(
    userAnswers: UserAnswers
  ): Call =
    IsThisFileConfidentialController.onPageLoad(
      NormalMode,
      userAnswers.draftId
    )

  private def isThisFileConfidentialPage(
    userAnswers: UserAnswers
  ): Call =
    UploadAnotherSupportingDocumentController
      .onPageLoad(NormalMode, userAnswers.draftId)

  private def uploadAnotherSupportingDocumentPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers
      .get(UploadAnotherSupportingDocumentPage)
      .map {
        case true  =>
          UploadSupportingDocumentsController.onPageLoad(
            NormalMode,
            userAnswers.draftId,
            None,
            None
          )
        case false => CheckYourAnswersController.onPageLoad(userAnswers.draftId)
      }
      .getOrElse(JourneyRecoveryController.onPageLoad())

  private def removeSupportingDocumentPage(userAnswers: UserAnswers): Call = {
    val numberOfDocuments = userAnswers.get(AllDocuments).map(_.size)

    numberOfDocuments match {
      case Some(count) if count > 0 =>
        UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) | None           =>
        DoYouWantToUploadDocumentsController.onPageLoad(NormalMode, userAnswers.draftId)
    }
  }

  private def importGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ImportGoodsPage) match {
      case None        => ImportGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  => ContactPageController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) => ImportingGoodsController.onPageLoad(userAnswers.draftId)
    }

  private def cancelApplicationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(CancelApplicationPage) match {
      case None    => CancelApplicationController.onPageLoad(userAnswers.draftId)
      case Some(_) => CancelApplicationController.confirmCancel(userAnswers.draftId)
    }

  private def whatIsYourRoleAsImporterPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None    => WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => RequiredInformationController.onPageLoad(userAnswers.draftId)

    }

  private def changeYourRoleImporterPageRoute(userAnswers: UserAnswers): Call =
    (userAnswers.get(ChangeYourRoleImporterPage), userAnswers.get(WhatIsYourRoleAsImporterPage)) match {
      case (None, None)          =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case (Some(_), None)       =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case (Some(true), Some(_)) =>
        RequiredInformationController.onPageLoad(userAnswers.draftId)
      case (Some(false), _)      =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
      case _                     =>
        ChangeYourRoleImporterController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def tellUsAboutYourRuling(userAnswers: UserAnswers): Call =
    userAnswers.get(TellUsAboutYourRulingPage) match {
      case None    => TellUsAboutYourRulingController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => AwareOfRulingController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def haveYouReceivedADecision(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouReceivedADecisionPage) match {
      case None        => HaveYouReceivedADecisionController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  => TellUsAboutYourRulingController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) => AwareOfRulingController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def awareOfRulingsWithSimilarMethod(userAnswers: UserAnswers): Call =
    userAnswers.get(AwareOfRulingPage) match {
      case None        => AwareOfRulingController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(true)  => AboutSimilarGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(false) => HasCommodityCodeController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def aboutSimilarGoods(userAnswers: UserAnswers): Call =
    userAnswers.get(AboutSimilarGoodsPage) match {
      case None    => AboutSimilarGoodsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => HasCommodityCodeController.onPageLoad(NormalMode, userAnswers.draftId)
    }

  private def contactsNextPage(userAnswers: UserAnswers): Call =
    userRoleProvider.getUserRole(userAnswers).getEORIDetailsJourney(NormalMode, userAnswers.draftId)

  private def checkRegisteredDetailsPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None        => CheckRegisteredDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(value) =>
        if (value) {
          userRoleProvider.getUserRole(userAnswers).getContactDetailsJourney(userAnswers.draftId)

        } else {
          EORIBeUpToDateController.onPageLoad(userAnswers.draftId)
        }
    }

  private def applicationContactDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ApplicationContactDetailsPage) match {
      case None    => ApplicationContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => ChoosingMethodController.onPageLoad(userAnswers.draftId)
    }

  private def agentCompanyDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AgentCompanyDetailsPage) match {
      case None    => AgentCompanyDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => ChoosingMethodController.onPageLoad(userAnswers.draftId)
    }

  private def uploadLetterOfAuthorityPage(userAnswers: UserAnswers): Call =
    VerifyLetterOfAuthorityController.onPageLoad(NormalMode, userAnswers.draftId)

  private def verifyLetterOfAuthorityPage(userAnswers: UserAnswers): Call =
    BusinessContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId)

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case CheckMode     =>
        CheckModeNavigator.nextPage(page, userAnswers)
      case UnchangedMode =>
        unchangedModeNavigator.nextPage(page, userAnswers)
      case _             =>
        routes(page)(userAnswers)
    }
}
