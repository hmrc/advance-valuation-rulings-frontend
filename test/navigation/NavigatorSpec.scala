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

    "in Normal mode" - {

      "RequiredInformationPage must" in {
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

//     TODO: Method Pages Navigation Spec should be down below

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
}
