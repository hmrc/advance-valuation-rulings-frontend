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
import models.ValuationMethod.Method1
import models.ValuationMethod.Method2
import pages._
import routes._

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ValuationMethodPage                 => valuationMethodPage
    case NameOfGoodsPage                     => nameOfGoodsPage
    case HasCommodityCodePage                => hasCommodityCodePage
    case CommodityCodePage                   => commodityCodePage
    case PriceOfGoodsPage                    => priceOfGoodsPage
    case WhatCountryAreGoodsFromPage         => whatCountryAreGoodsFromPage
    case AreGoodsShippedDirectlyPage         => areGoodsShippedDirectlyPage
    case DescribeTheGoodsPage                => describeTheGoodsPage
    case HowAreTheGoodsMadePage              => howAreTheGoodsMadePage
    case HasConfidentialInformationPage      => hasConfidentialInformationPage
    case ConfidentialInformationPage         => confidentialInformationPage
    case ImportGoodsPage                     => importGoodsPage
    case RequiredInformationPage             => requiredInformationPage
    case CheckRegisteredDetailsPage          => checkRegisteredDetailsPage
    case ApplicationContactDetailsPage       => applicationContactDetailsPage
    case DoYouWantToUploadDocumentsPage      => doYouWantToUploadDocumentsPage
    case IsThisFileConfidentialPage          => isThisFileConfidentialPage
    case UploadAnotherSupportingDocumentPage => uploadAnotherSupportingDocumentPage

    case _ => _ => routes.IndexController.onPageLoad
  }

  private def valuationMethodPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ValuationMethodPage) match {
      case None                  => ValuationMethodController.onPageLoad(models.NormalMode)
      case Some(valuationMethod) =>
        valuationMethod match {
          case Method1 => NameOfGoodsController.onPageLoad(models.NormalMode)
          case Method2 => NameOfGoodsController.onPageLoad(models.NormalMode)
        }
    }

  private def nameOfGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(NameOfGoodsPage) match {
      case None    => NameOfGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => HasCommodityCodeController.onPageLoad(models.NormalMode)
    }

  private def hasCommodityCodePage(userAnswers: UserAnswers): Call =
    userAnswers.get(HasCommodityCodePage) match {
      case None        => HasCommodityCodeController.onPageLoad(models.NormalMode)
      case Some(true)  => CommodityCodeController.onPageLoad(models.NormalMode)
      case Some(false) => MustHaveCommodityCodeController.onPageLoad()
    }

  private def commodityCodePage(userAnswers: UserAnswers): Call =
    userAnswers.get(CommodityCodePage) match {
      case None    => CommodityCodeController.onPageLoad(models.NormalMode)
      case Some(_) => WhatCountryAreGoodsFromController.onPageLoad(models.NormalMode)
    }

  // todo: page removed from prototype
  private def priceOfGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(PriceOfGoodsPage) match {
      case None    => PriceOfGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => DescribeTheGoodsController.onPageLoad(models.NormalMode)
    }

  private def whatCountryAreGoodsFromPage(userAnswers: UserAnswers): Call =
    userAnswers.get(WhatCountryAreGoodsFromPage) match {
      case None    => WhatCountryAreGoodsFromController.onPageLoad(models.NormalMode)
      case Some(_) => AreGoodsShippedDirectlyController.onPageLoad(models.NormalMode)
    }
  private def areGoodsShippedDirectlyPage(userAnswers: UserAnswers): Call =
    userAnswers.get(AreGoodsShippedDirectlyPage) match {
      case None    => AreGoodsShippedDirectlyController.onPageLoad(models.NormalMode)
      case Some(_) => DescribeTheGoodsController.onPageLoad(models.NormalMode)
    }

  private def describeTheGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheGoodsPage) match {
      case None    => DescribeTheGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => HowAreTheGoodsMadeController.onPageLoad(models.NormalMode)
    }

  private def howAreTheGoodsMadePage(userAnswers: UserAnswers): Call =
    userAnswers.get(DescribeTheGoodsPage) match {
      case None    => HowAreTheGoodsMadeController.onPageLoad(models.NormalMode)
      case Some(_) => HasConfidentialInformationController.onPageLoad(models.NormalMode)
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
      case Some(true)  => UploadSupportingDocumentsController.onPageLoad
      case Some(false) => routes.IndexController.onPageLoad
    }

  private def isThisFileConfidentialPage(userAnswers: UserAnswers): Call =
    userAnswers.get(IsThisFileConfidentialPage) match {
      case None    => IsThisFileConfidentialController.onPageLoad(models.NormalMode)
      case Some(_) => UploadAnotherSupportingDocumentController.onPageLoad(NormalMode)
    }

  private def uploadAnotherSupportingDocumentPage(userAnswers: UserAnswers): Call =
    userAnswers.get(UploadAnotherSupportingDocumentPage) match {
      case None        => UploadAnotherSupportingDocumentController.onPageLoad(models.NormalMode)
      case Some(true)  => UploadSupportingDocumentsController.onPageLoad
      case Some(false) => routes.IndexController.onPageLoad
    }

  private def importGoodsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ImportGoodsPage) match {
      case None        => ImportGoodsController.onPageLoad(models.NormalMode)
      case Some(true)  => PublicInformationNoticeController.onPageLoad()
      case Some(false) => ImportingGoodsController.onPageLoad()
    }

  private def requiredInformationPage(userAnswers: UserAnswers): Call =
    userAnswers.get(RequiredInformationPage) match {
      case None    => RequiredInformationController.onPageLoad()
      case Some(_) => ImportGoodsController.onPageLoad(models.NormalMode)
    }

  private def checkRegisteredDetailsPage(userAnswers: UserAnswers): Call =
    userAnswers.get(CheckRegisteredDetailsPage) match {
      case None                             => CheckRegisteredDetailsController.onPageLoad(models.NormalMode)
      case Some(CheckRegisteredDetails.Yes) =>
        ApplicationContactDetailsController.onPageLoad(models.NormalMode)
      case Some(CheckRegisteredDetails.No)  => EORIBeUpToDateController.onPageLoad()
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
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)
  }
}
