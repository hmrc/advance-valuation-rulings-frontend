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

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call

import controllers.routes
import models._
import pages._
import routes._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
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
    case CheckRegisteredDetailsPage                       => checkRegisteredDetailsPage
    case ApplicationContactDetailsPage                    => applicationContactDetailsPage
    case DoYouWantToUploadDocumentsPage                   => doYouWantToUploadDocumentsPage
    case IsThisFileConfidentialPage                       => isThisFileConfidentialPage
    case UploadAnotherSupportingDocumentPage              => uploadAnotherSupportingDocumentPage
    case WhyComputedValuePage                             => whyComputedValuePage
    case ExplainReasonComputedValuePage                   => explainReasonComputedValuePage
    case WhyTransactionValueOfSimilarGoodsPage            => whyTransactionValueOfSimilarGoodsPage
    case HaveYouUsedMethodOneInPastPage                   => haveYouUsedMethodOneInPastPage
    case WhyIdenticalGoodsPage                            => whyIdenticalGoodsPage
    case AreThereRestrictionsOnTheGoodsPage               => areThereRestrictionsOnTheGoodsPage
    case DescribeTheRestrictionsPage                      => describeTheRestrictionsPage
    case IsTheSaleSubjectToConditionsPage                 => isTheSaleSubjectToConditionsPage
    case DescribeTheConditionsPage                        => describeTheConditionsPage
    case DescribeTheIdenticalGoodsPage                    => describeTheIdenticalGoodsPage
    case ExplainYourGoodsComparingToIdenticalGoodsPage    =>
      explainYourGoodsComparingToIdenticalGoodsPage
    case WillYouCompareGoodsToIdenticalGoodsPage          => willYouCompareGoodsToIdenticalGoodsPage
    case ExplainWhyYouHaveNotSelectedMethodOneToThreePage =>
      explainWhyYouHaveNotSelectedMethodOneToThreePage
    case ExplainWhyYouChoseMethodFourPage                 => explainWhyYouChoseMethodFourPage
    case ExplainWhyYouHaveNotSelectedMethodOneToFivePage  =>
      explainWhyYouHaveNotSelectedMethodOneToFivePage
    case ExplainHowYouWillUseMethodSixPage                => explainHowYouWillUseMethodSixPage
    case AdaptMethodPage                                  => adaptMethodPage
    case _                                                => _ => routes.IndexController.onPageLoad
  }

  private def valuationMethodPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None                  => ValuationMethodController.onPageLoad(models.NormalMode)
      case Some(valuationMethod) =>
        import models.ValuationMethod._
        valuationMethod match {
          case Method1 => IsThereASaleInvolvedController.onPageLoad(models.NormalMode)
          case Method2 => WhyIdenticalGoodsController.onPageLoad(models.NormalMode)
          case Method3 => WhyTransactionValueOfSimilarGoodsController.onPageLoad(models.NormalMode)
          case Method4 =>
            ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(models.NormalMode)
          case Method5 => WhyComputedValueController.onPageLoad(models.NormalMode)
          case Method6 =>
            ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(models.NormalMode)
        }
    }
  // Method 1----------------------------------------------------------------

  private def areThereRestrictionsOnTheGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AreThereRestrictionsOnTheGoodsPage) match {
      case None        => AreThereRestrictionsOnTheGoodsController.onPageLoad(models.NormalMode)
      case Some(true)  => DescribeTheRestrictionsController.onPageLoad(models.NormalMode)
      case Some(false) => IsTheSaleSubjectToConditionsController.onPageLoad(models.NormalMode)
    }

  private def describeTheRestrictionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheRestrictionsPage) match {
      case None    => DescribeTheRestrictionsController.onPageLoad(models.NormalMode)
      case Some(_) => IsTheSaleSubjectToConditionsController.onPageLoad(models.NormalMode)
    }

  private def isTheSaleSubjectToConditionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsTheSaleSubjectToConditionsPage) match {
      case None        => IsTheSaleSubjectToConditionsController.onPageLoad(models.NormalMode)
      case Some(true)  => DescribeTheConditionsController.onPageLoad(models.NormalMode)
      case Some(false) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  private def describeTheConditionsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheConditionsPage) match {
      case None    => DescribeTheConditionsController.onPageLoad(models.NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  // Method 1----------------------------------------------------------------

  private def isThereASaleInvolvedPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsThereASaleInvolvedPage) match {
      case None        => IsThereASaleInvolvedController.onPageLoad(models.NormalMode)
      case Some(true)  => IsSaleBetweenRelatedPartiesController.onPageLoad(models.NormalMode)
      case Some(false) =>
        ValuationMethodController.onPageLoad(
          models.NormalMode
        )
    }

  private def isSaleBetweenRelatedPartiesPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsSaleBetweenRelatedPartiesPage) match {
      case None        => IsSaleBetweenRelatedPartiesController.onPageLoad(models.NormalMode)
      case Some(true)  => ExplainHowPartiesAreRelatedController.onPageLoad(models.NormalMode)
      case Some(false) => AreThereRestrictionsOnTheGoodsController.onPageLoad(models.NormalMode)
    }

  private def explainHowPartiesAreRelatedPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainHowPartiesAreRelatedPage) match {
      case None    => ExplainHowPartiesAreRelatedController.onPageLoad(models.NormalMode)
      case Some(_) => AreThereRestrictionsOnTheGoodsController.onPageLoad(models.NormalMode)
    }

  // Method 2----------------------------------------------------------------
  private def whyIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyIdenticalGoodsPage) match {
      case None    => WhyIdenticalGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => HaveYouUsedMethodOneInPastController.onPageLoad(models.NormalMode)
    }

  private def haveYouUsedMethodOneInPastPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveYouUsedMethodOneInPastPage) match {
      case None        => HaveYouUsedMethodOneInPastController.onPageLoad(models.NormalMode)
      case Some(true)  => DescribeTheIdenticalGoodsController.onPageLoad(models.NormalMode)
      case Some(false) =>
        WillYouCompareGoodsToIdenticalGoodsController.onPageLoad(models.NormalMode)
    }

  private def describeTheIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheIdenticalGoodsPage) match {
      case None    => DescribeTheIdenticalGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  private def willYouCompareGoodsToIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WillYouCompareGoodsToIdenticalGoodsPage) match {
      case None        => WillYouCompareGoodsToIdenticalGoodsController.onPageLoad(models.NormalMode)
      case Some(true)  =>
        ExplainYourGoodsComparingToIdenticalGoodsController.onPageLoad(models.NormalMode)
      case Some(false) => ValuationMethodController.onPageLoad(models.NormalMode)
    }

  private def explainYourGoodsComparingToIdenticalGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainYourGoodsComparingToIdenticalGoodsPage) match {
      case None    => ExplainYourGoodsComparingToIdenticalGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  // Method 3----------------------------------------------------------------
  private def whyTransactionValueOfSimilarGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyTransactionValueOfSimilarGoodsPage) match {
      case None    => WhyTransactionValueOfSimilarGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => HaveYouUsedMethodOneInPastController.onPageLoad(models.NormalMode)
    }

  // method 4 ----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToThreePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToThreePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToThreeController.onPageLoad(models.NormalMode)
      case Some(_) => ExplainWhyYouChoseMethodFourController.onPageLoad(models.NormalMode)
    }

  private def explainWhyYouChoseMethodFourPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainWhyYouChoseMethodFourPage) match {
      case None    => ExplainWhyYouChoseMethodFourController.onPageLoad(models.NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  // method 5----------------------------------------------------------------
  private def whyComputedValuePage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhyComputedValuePage) match {
      case None    => WhyComputedValueController.onPageLoad(models.NormalMode)
      case Some(_) => ExplainReasonComputedValueController.onPageLoad(models.NormalMode)
    }

  private def explainReasonComputedValuePage(userAnswers: UserAnswers): Call =
    userAnswers.get(ExplainReasonComputedValuePage) match {
      case None    => ExplainReasonComputedValueController.onPageLoad(models.NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  // method 6----------------------------------------------------------------
  private def explainWhyYouHaveNotSelectedMethodOneToFivePage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ExplainWhyYouHaveNotSelectedMethodOneToFivePage) match {
      case None    =>
        ExplainWhyYouHaveNotSelectedMethodOneToFiveController.onPageLoad(models.NormalMode)
      case Some(_) => AdaptMethodController.onPageLoad(models.NormalMode)
    }

  private def adaptMethodPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(AdaptMethodPage) match {
      case None    => AdaptMethodController.onPageLoad(models.NormalMode)
      case Some(_) => ExplainHowYouWillUseMethodSixController.onPageLoad(models.NormalMode)
    }

  private def explainHowYouWillUseMethodSixPage(
    userAnswers: UserAnswers
  ): Call =
    userAnswers.get(ExplainHowYouWillUseMethodSixPage) match {
      case None    => ExplainHowYouWillUseMethodSixController.onPageLoad(models.NormalMode)
      case Some(_) => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
    }

  // ----------------------------------------------------------------
  private def descriptionOfGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescriptionOfGoodsPage) match {
      case None    => DescriptionOfGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => HasCommodityCodeController.onPageLoad(models.NormalMode)
    }

  private def hasCommodityCodePage(userAnswers: UserAnswers): Call                         =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(models.NormalMode)
      case Some(true)  => CommodityCodeController.onPageLoad(models.NormalMode)
      case Some(false) =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(models.NormalMode)
    }
  private def haveTheGoodsBeenSubjectToLegalChallengesPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage) match {
      case None        => HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(models.NormalMode)
      case Some(true)  => DescribeTheLegalChallengesController.onPageLoad(models.NormalMode)
      case Some(false) =>
        HasConfidentialInformationController.onPageLoad(models.NormalMode)
    }
  private def describeTheLegalChallengesPage(userAnswers: UserAnswers): Call               =
    userAnswers.get(DescribeTheLegalChallengesPage) match {
      case None    => DescribeTheLegalChallengesController.onPageLoad(models.NormalMode)
      case Some(_) => HasConfidentialInformationController.onPageLoad(models.NormalMode)

    }

  private def commodityCodePage(userAnswers: UserAnswers): Call =
    userAnswers.get(CommodityCodePage) match {
      case None    => CommodityCodeController.onPageLoad(models.NormalMode)
      case Some(_) =>
        HaveTheGoodsBeenSubjectToLegalChallengesController.onPageLoad(models.NormalMode)
    }

  private def hasConfidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(HasConfidentialInformationPage) match {
      case None        => HasConfidentialInformationController.onPageLoad(models.NormalMode)
      case Some(true)  => ConfidentialInformationController.onPageLoad(models.NormalMode)
      case Some(false) => DoYouWantToUploadDocumentsController.onPageLoad(models.NormalMode)
    }

  private def confidentialInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ConfidentialInformationPage) match {
      case None    => ConfidentialInformationController.onPageLoad(models.NormalMode)
      case Some(_) => DoYouWantToUploadDocumentsController.onPageLoad(models.NormalMode)
    }

  private def doYouWantToUploadDocumentsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DoYouWantToUploadDocumentsPage) match {
      case None        => DoYouWantToUploadDocumentsController.onPageLoad(models.NormalMode)
      case Some(true)  =>
        controllers.fileupload.routes.UploadSupportingDocumentsController
          .onPageLoad(None, None, None)
      case Some(false) => routes.CheckYourAnswersController.onPageLoad
    }

  private def isThisFileConfidentialPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsThisFileConfidentialPage) match {
      case None    => IsThisFileConfidentialController.onPageLoad(models.NormalMode)
      case Some(_) => UploadAnotherSupportingDocumentController.onPageLoad(models.NormalMode)
    }

  private def uploadAnotherSupportingDocumentPage(userAnswers: UserAnswers): Call =
    userAnswers.get(UploadAnotherSupportingDocumentPage) match {
      case None        => UploadAnotherSupportingDocumentController.onPageLoad(models.NormalMode)
      case Some(true)  =>
        controllers.fileupload.routes.UploadSupportingDocumentsController
          .onPageLoad(None, None, None)
      case Some(false) => routes.CheckYourAnswersController.onPageLoad
    }

  private def importGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ImportGoodsPage) match {
      case None        => ImportGoodsController.onPageLoad(models.NormalMode)
      case Some(true)  => ContactPageController.onPageLoad()
      case Some(false) => ImportingGoodsController.onPageLoad()
    }

  private def requiredInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(RequiredInformationPage) match {
      case None    => RequiredInformationController.onPageLoad()
      case Some(_) => ImportGoodsController.onPageLoad(models.NormalMode)
    }

  private def checkRegisteredDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None        => CheckRegisteredDetailsController.onPageLoad(models.NormalMode)
      case Some(true)  =>
        ApplicationContactDetailsController.onPageLoad(models.NormalMode)
      case Some(false) => EORIBeUpToDateController.onPageLoad()
    }

  private def applicationContactDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ApplicationContactDetailsPage) match {
      case None    => ApplicationContactDetailsController.onPageLoad(models.NormalMode)
      case Some(_) => ValuationMethodController.onPageLoad(models.NormalMode)
    }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case models.NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode         =>
      checkRouteMap(page)(userAnswers)
  }
}
