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
import models.{BusinessContactDetails, CheckMode, UserAnswers, WhatIsYourRoleAsImporter}
import pages.{BusinessContactDetailsPage, WhatIsYourRoleAsImporterPage}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessContactDetailsSummary {

  private def nameRow(answer: BusinessContactDetails, role: WhatIsYourRoleAsImporter)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = getMessageKey(role, "name"),
      value = ValueViewModel(HtmlFormat.escape(answer.name).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages(getAriaMessageKey(role, "name")))
      )
    )

  private def emailRow(answer: BusinessContactDetails, role: WhatIsYourRoleAsImporter)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = getMessageKey(role, "email"),
      value = ValueViewModel(HtmlFormat.escape(answer.email).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages(getAriaMessageKey(role, "email")))
      )
    )
  private def contactNumberRow(answer: BusinessContactDetails, role: WhatIsYourRoleAsImporter)(
    implicit messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = getMessageKey(role, "phone"),
      value = ValueViewModel(HtmlFormat.escape(answer.phone).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages(getAriaMessageKey(role, "phone")))
      )
    )

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(BusinessContactDetailsPage)
      role           <- userAnswer.get(WhatIsYourRoleAsImporterPage)
      name            = nameRow(contactDetails, role)
      email           = emailRow(contactDetails, role)
      contactNumber   = contactNumberRow(contactDetails, role)
      result          = Seq(name, email, contactNumber)
    } yield result

  private def getMessageKey(role: WhatIsYourRoleAsImporter, fieldName: String): String =
    role match {
      case WhatIsYourRoleAsImporter.EmployeeOfOrg      =>
        s"checkYourAnswersForAgents.applicant.$fieldName.label"
      case WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg =>
        s"checkYourAnswersForAgents.agent.org.$fieldName.label"
    }

  private def getAriaMessageKey(role: WhatIsYourRoleAsImporter, fieldName: String): String =
    role match {
      case WhatIsYourRoleAsImporter.EmployeeOfOrg      =>
        s"businessContactDetails.$fieldName.change.hidden"
      case WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg =>
        s"businessContactDetails.agent.org.$fieldName.change.hidden"
    }

}
