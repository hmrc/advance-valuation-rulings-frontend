/*
 * Copyright 2024 HM Revenue & Customs
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

class UnchangedModeNavigator {

  private def whatIsYourRoleAsImporter(userAnswers: UserAnswers): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None    =>
        WhatIsYourRoleAsImporterController.onPageLoad(UnchangedMode, userAnswers.draftId)
      case Some(_) =>
        CheckYourAnswersController.onPageLoad(userAnswers.draftId)
    }

  private def changeYourRoleImporterPage(userAnswers: UserAnswers): Call =
    userAnswers.get(ChangeYourRoleImporterPage) match {
      case None        =>
        ChangeYourRoleImporterController.onPageLoad(UnchangedMode, userAnswers.draftId)
      case Some(true)  =>
        RequiredInformationController.onPageLoad(userAnswers.draftId)
      case Some(false) =>
        CheckYourAnswersController.onPageLoad(draftId = userAnswers.draftId)
    }

  def nextPage(page: Page, userAnswers: UserAnswers): Call =
    page match {
      case WhatIsYourRoleAsImporterPage => whatIsYourRoleAsImporter(userAnswers)
      case ChangeYourRoleImporterPage   => changeYourRoleImporterPage(userAnswers)
      case _                            => CheckYourAnswersController.onPageLoad(draftId = userAnswers.draftId)
    }
}
