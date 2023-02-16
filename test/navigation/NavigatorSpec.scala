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

import base.SpecBase
import controllers.routes
import models._
import pages._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "must go from a page that doesn't exist in the route map to Index" in {
      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        NormalMode,
        UserAnswers("id")
      ) mustBe routes.IndexController.onPageLoad
    }

    "in Normal mode" - {

      "WhyTransactionValueOfSimilarGoods page" - {

        "must navigate to HaveYouUsedMethodOneInPast page" in {
          val userAnswers =
            UserAnswers("id").set(WhyTransactionValueOfSimilarGoodsPage, "bananas").get
          navigator.nextPage(
            WhyTransactionValueOfSimilarGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(NormalMode)

        }
      }

      "HaveYouUsedMethodOneInPast page" - {
        "must navigate to nameOfGoods Page" in {
          val userAnswers =
            UserAnswers("id").set(HaveYouUsedMethodOneInPastPage, true).get
          navigator.nextPage(
            HaveYouUsedMethodOneInPastPage,
            NormalMode,
            userAnswers
          ) mustBe routes.NameOfGoodsController.onPageLoad(NormalMode)
        }
      }

      "RequiredInformationPage must" - {
        "navigate to Import goods page when all values are set" in {
          val userAnswers =
            UserAnswers("id")
              .set(RequiredInformationPage, RequiredInformation.values.toSet)
              .get
          navigator.nextPage(
            RequiredInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ImportGoodsController.onPageLoad(mode = NormalMode)
        }

        "navigate to self when no values are set" in {
          val userAnswers = UserAnswers("id")
          navigator.nextPage(
            RequiredInformationPage,
            NormalMode,
            userAnswers
          ) mustBe routes.RequiredInformationController.onPageLoad
        }
      }

      "ImportGoodsPage must" - {

        "navigate to PublicInformationNoticePage when True" in {
          val userAnswers =
            UserAnswers("id")
              .set(ImportGoodsPage, true)
              .get
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.PublicInformationNoticeController.onPageLoad()
        }

        "and navigate to ImportingGoodsPage when False" in {
          val userAnswers =
            UserAnswers("id")
              .set(ImportGoodsPage, false)
              .get
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ImportingGoodsController.onPageLoad()
        }

        "navigate to ImportingGoodsPage when no value is set" in {
          val userAnswers = UserAnswers("id")
          navigator.nextPage(
            ImportGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ImportGoodsController.onPageLoad(mode = NormalMode)
        }
      }

      "CheckRegisteredDetailsPage must" - {

        "navigate to ApplicationContactDetailsPage when Yes" in {
          val userAnswers =
            UserAnswers("id").set(CheckRegisteredDetailsPage, CheckRegisteredDetails.Yes).get
          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ApplicationContactDetailsController.onPageLoad(mode = NormalMode)
        }

        "and navigate to EORIBeUpToDatePage when No" in {
          val userAnswers =
            UserAnswers("id").set(CheckRegisteredDetailsPage, CheckRegisteredDetails.No).get
          navigator.nextPage(
            CheckRegisteredDetailsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.EORIBeUpToDateController.onPageLoad
        }
      }

      "ApplicationContactDetailsPage must" in {
        val userAnswers =
          UserAnswers("id")
            .set(ApplicationContactDetailsPage, ApplicationContactDetails("name", "email", "phone"))
            .get
        navigator.nextPage(
          ApplicationContactDetailsPage,
          NormalMode,
          userAnswers
        ) mustBe routes.ValuationMethodController.onPageLoad(mode = NormalMode)
      }

      "DoYouWantToUploadDocumentsPage must" - {
        "self when no method is select" in {
          val userAnswers = UserAnswers("id")
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.DoYouWantToUploadDocumentsController.onPageLoad(mode = NormalMode)
        }

        "UploadSupportingDocumentsPage when Yes is selected" in {
          val userAnswers =
            UserAnswers("id").set(DoYouWantToUploadDocumentsPage, true).get
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe controllers.fileupload.routes.UploadSupportingDocumentsController
            .onPageLoad(None, None, None)
        }

        "IndexPage when No is selected" in {
          val userAnswers =
            UserAnswers("id").set(DoYouWantToUploadDocumentsPage, false).get
          navigator.nextPage(
            DoYouWantToUploadDocumentsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.IndexController.onPageLoad
        }
      }

      "IsThisFileConfidentialPage must" - {
        "self when no method is select" in {
          val userAnswers = UserAnswers("id")
          navigator.nextPage(
            IsThisFileConfidentialPage,
            NormalMode,
            userAnswers
          ) mustBe routes.IsThisFileConfidentialController.onPageLoad(mode = NormalMode)
        }

        "UploadSupportingDocumentsPage when an answer is selected" in {
          val userAnswers =
            UserAnswers("id").set(IsThisFileConfidentialPage, false).get
          navigator.nextPage(
            IsThisFileConfidentialPage,
            NormalMode,
            userAnswers
          ) mustBe routes.UploadAnotherSupportingDocumentController.onPageLoad(NormalMode)
        }
      }

      "valuationMethod page must navigate to" - {
        "self when no method is selected" in {
          val userAnswers = UserAnswers("id")
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.ValuationMethodController.onPageLoad(mode = NormalMode)
        }

        "NameOfGoods page when method 1 is selected" in {
          val userAnswers = UserAnswers("id").set(ValuationMethodPage, ValuationMethod.Method1).get
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.NameOfGoodsController.onPageLoad(mode = NormalMode)
        }

        "WhyIdenticalGoods page when method 2 is selected" in {
          val userAnswers = UserAnswers("id").set(ValuationMethodPage, ValuationMethod.Method2).get
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(mode = NormalMode)
        }

        "WhyTransactionValueOfSimilarGoods page when method 3 is selected" in {
          val userAnswers = UserAnswers("id").set(ValuationMethodPage, ValuationMethod.Method3).get
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyTransactionValueOfSimilarGoodsController.onPageLoad(mode = NormalMode)
        }

        "NameOfGoods page when method 4 is selected" in {
          val userAnswers = UserAnswers("id").set(ValuationMethodPage, ValuationMethod.Method4).get
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.NameOfGoodsController.onPageLoad(mode = NormalMode)
        }

        "WhyComputedValue page when method 5 is selected" in {
          val userAnswers = UserAnswers("id").set(ValuationMethodPage, ValuationMethod.Method5).get
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyComputedValueController.onPageLoad(mode = NormalMode)
        }

        "NameOfGoods page when method 6 is selected" in {
          val userAnswers = UserAnswers("id").set(ValuationMethodPage, ValuationMethod.Method6).get
          navigator.nextPage(
            ValuationMethodPage,
            NormalMode,
            userAnswers
          ) mustBe routes.NameOfGoodsController.onPageLoad(mode = NormalMode)
        }
      }

      "HasCommodityCodePage must" - {
        "navigate to CommodityCode when yes" in {
          navigator.nextPage(
            HasCommodityCodePage,
            NormalMode,
            UserAnswers("id").set(HasCommodityCodePage, true).success.value
          ) mustBe routes.CommodityCodeController.onPageLoad(NormalMode)
        }
      }

      "CommodityCode must" - {
        "navigate to WhatCountryAreGoodsFrom when set" in {
          navigator.nextPage(
            CommodityCodePage,
            NormalMode,
            UserAnswers("id").set(CommodityCodePage, "1234567890").success.value
          ) mustBe routes.WhatCountryAreGoodsFromController.onPageLoad(NormalMode)
        }
      }

      "WhatCountryAreGoodsFrom must" - {
        "navigate to AreGoodsShippedDirectly" in {
          navigator.nextPage(
            WhatCountryAreGoodsFromPage,
            NormalMode,
            UserAnswers("id").set(WhatCountryAreGoodsFromPage, "GB").success.value
          ) mustBe routes.AreGoodsShippedDirectlyController.onPageLoad(NormalMode)
        }
      }

      "AreGoodsShippedDirectly must" - {
        "navigate to DescribeTheGoods when true" in {
          navigator.nextPage(
            AreGoodsShippedDirectlyPage,
            NormalMode,
            UserAnswers("id").set(AreGoodsShippedDirectlyPage, true).success.value
          ) mustBe routes.DescribeTheGoodsController.onPageLoad(NormalMode)
        }
      }

      "DescribeTheGoods page must" - {
        "navigate to HowAreTheGoodsMade when given valid data" in {
          navigator.nextPage(
            DescribeTheGoodsPage,
            NormalMode,
            UserAnswers("id").set(DescribeTheGoodsPage, "Some goods").success.value
          ) mustBe routes.HowAreTheGoodsMadeController.onPageLoad(NormalMode)
        }
      }

      "whyIdenticalGoods Page must" - {
        "must go to HaveYouUsedMethodOneInPastPage" in {
          val userAnswers = UserAnswers("id").set(WhyIdenticalGoodsPage, "banana").get
          navigator.nextPage(
            WhyIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.HaveYouUsedMethodOneInPastController.onPageLoad(mode = NormalMode)
        }

        "must navigate to itself" in {
          val userAnswers = UserAnswers("id")
          navigator.nextPage(
            WhyIdenticalGoodsPage,
            NormalMode,
            userAnswers
          ) mustBe routes.WhyIdenticalGoodsController.onPageLoad(mode = NormalMode)
        }
      }

      "whyComputedValue Page must" - {
        "must go to explainReasonComputedValuePage" in {
          val userAnswers = UserAnswers("id").set(WhyComputedValuePage, "banana").get
          navigator.nextPage(
            WhyComputedValuePage,
            NormalMode,
            userAnswers
          ) mustBe routes.ExplainReasonComputedValueController.onPageLoad(mode = NormalMode)
        }
      }
    }

  }

  "in Check mode" - {

    "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

      case object UnknownPage extends Page
      navigator.nextPage(
        UnknownPage,
        CheckMode,
        UserAnswers("id")
      ) mustBe routes.CheckYourAnswersController.onPageLoad
    }
  }
}
