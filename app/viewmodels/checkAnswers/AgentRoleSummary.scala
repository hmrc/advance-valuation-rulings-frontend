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

package viewmodels.checkAnswers

import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models.{CheckMode, UserAnswers, WhatIsYourRoleAsImporter}
import models.requests.ImporterRole
import models.requests.ImporterRole.AgentOnBehalf
import models.requests.ImporterRole.Employee
import pages.WhatIsYourRoleAsImporterPage
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentRoleSummary {

  def row(role: WhatIsYourRoleAsImporter)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.applicant.role.label",
      value =
        ValueViewModel(HtmlFormat.escape(messages(s"whatIsYourRoleAsImporter.$role")).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.WhatIsYourRoleAsImporterController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages("businessContactDetails.role.change.hidden"))
      )
    )

  def row(role: ImporterRole)(implicit messages: Messages): SummaryListRow =
    role match {
      case AgentOnBehalf => row(WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
      case Employee      => row(WhatIsYourRoleAsImporter.EmployeeOfOrg)
    }

  def row(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    userAnswer.get(WhatIsYourRoleAsImporterPage).map(role => Seq(row(role)))
}
