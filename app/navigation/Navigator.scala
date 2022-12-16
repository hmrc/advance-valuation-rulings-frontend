/*
 * Copyright 2022 HM Revenue & Customs
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
import pages._
import models._
import models.ValuationMethod.Method1
import models.ValuationMethod.Method2
import routes.{CommodityCodeController, HasCommodityCodeController, NameOfGoodsController, ValuationMethodController}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ValuationMethodPage => valuationMethodPage
    case NameOfGoodsPage => nameOfGoodsPage
    case HasCommodityCodePage => hasCommodityCodePage
    case CommodityCodePage => _ => routes.IndexController.onPageLoad
    case _ => _ => routes.IndexController.onPageLoad
  }

  private def valuationMethodPage(userAnswers: UserAnswers): Call = {
    userAnswers.get(ValuationMethodPage) match {
      case None => ValuationMethodController.onPageLoad(models.NormalMode)
      case Some(valuationMethod) => valuationMethod match {
        case Method1 => NameOfGoodsController.onPageLoad(models.NormalMode)
        case Method2 => NameOfGoodsController.onPageLoad(models.NormalMode)
      }
    }
  }

  private def nameOfGoodsPage(userAnswers: UserAnswers): Call = {
    userAnswers.get(NameOfGoodsPage) match {
      case None => NameOfGoodsController.onPageLoad(models.NormalMode)
      case Some(_) => HasCommodityCodeController.onPageLoad(models.NormalMode)
    }
  }

  private def hasCommodityCodePage(userAnswers: UserAnswers): Call = {
    userAnswers.get(HasCommodityCodePage) match {
      case None => HasCommodityCodeController.onPageLoad(models.NormalMode)
      case Some(true) => CommodityCodeController.onPageLoad(models.NormalMode)
      case Some(false) => routes.IndexController.onPageLoad
    }
  }


  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
