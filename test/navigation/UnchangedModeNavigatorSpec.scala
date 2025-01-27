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

import base.SpecBase
import controllers.routes
import models.WhatIsYourRoleAsImporter.EmployeeOfOrg
import models._
import pages._
import play.api.libs.json.Writes
import queries._

class UnchangedModeNavigatorSpec extends SpecBase {

  private val userAnswers: UserAnswers                       = userAnswersAsIndividualTrader
  private val unchangedModeNavigator: UnchangedModeNavigator = new UnchangedModeNavigator

  "UnchangedModeNavigator" - {

    def userAnswersWith[A: Writes](page: Modifiable[A], value: A): UserAnswers =
      userAnswers.set(page, value).success.value

    "must navigate to CheckYourAnswersPage when user has answered for the WhatIsYourRoleAsImporterPage" in {

      val userAnswers: UserAnswers = userAnswersWith(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
      unchangedModeNavigator.nextPage(
        WhatIsYourRoleAsImporterPage,
        userAnswers
      ) mustBe routes.CheckYourAnswersController.onPageLoad(userAnswers.draftId)
    }

    "must navigate to RequiredInformationPage when user has answered true for the ChangeYourRoleImporterPage" in {

      val userAnswers: UserAnswers = userAnswersWith(ChangeYourRoleImporterPage, true)
      unchangedModeNavigator.nextPage(
        ChangeYourRoleImporterPage,
        userAnswers
      ) mustBe routes.RequiredInformationController.onPageLoad(userAnswers.draftId)
    }

    "must navigate to CheckYourAnswersPage when user has answered false for the ChangeYourRoleImporterPage" in {

      val userAnswers: UserAnswers = userAnswersWith(ChangeYourRoleImporterPage, false)
      unchangedModeNavigator.nextPage(
        ChangeYourRoleImporterPage,
        userAnswers
      ) mustBe routes.CheckYourAnswersController.onPageLoad(userAnswers.draftId)
    }
  }
}
