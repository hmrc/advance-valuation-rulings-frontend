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
import models.{BusinessContactDetails, CheckMode, DraftId, UserAnswers, WhatIsYourRoleAsImporter}
import pages.{BusinessContactDetailsPage, WhatIsYourRoleAsImporterPage}
import viewmodels.checkAnswers.BusinessContactDetailsSummary.contactNumberRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessContactDetailsSummary {

  private def nameRow(
    answer: BusinessContactDetails,
    role: WhatIsYourRoleAsImporter,
    draftId: DraftId
  )(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = getMessageKey(role, "name"),
      value = ValueViewModel(HtmlFormat.escape(answer.name).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages(getAriaMessageKey(role, "name")))
      )
    )

  private def emailRow(
    answer: BusinessContactDetails,
    role: WhatIsYourRoleAsImporter,
    draftId: DraftId
  )(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = getMessageKey(role, "email"),
      value = ValueViewModel(HtmlFormat.escape(answer.email).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages(getAriaMessageKey(role, "email")))
      )
    )

  private def contactNumberRow(
    answer: BusinessContactDetails,
    role: WhatIsYourRoleAsImporter,
    draftId: DraftId
  )(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = getMessageKey(role, "phone"),
      value = ValueViewModel(HtmlFormat.escape(answer.phone).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages(getAriaMessageKey(role, "phone")))
      )
    )

  private def companyNameRow(
    answer: BusinessContactDetails,
    role: WhatIsYourRoleAsImporter,
    draftId: DraftId
  )(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "agentForTraderCheckYourAnswers.applicant.companyName.label",
      value = ValueViewModel(HtmlFormat.escape(answer.companyName.getOrElse("")).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText("agentForTraderCheckYourAnswers.applicant.companyName.hidden")
      )
    )

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(BusinessContactDetailsPage)
      role           <- userAnswer.get(WhatIsYourRoleAsImporterPage)
      name            = nameRow(contactDetails, role, userAnswer.draftId)
      email           = emailRow(contactDetails, role, userAnswer.draftId)
      contactNumber   = contactNumberRow(contactDetails, role, userAnswer.draftId)
      companyName     = companyNameRow(contactDetails, role, userAnswer.draftId)
      result          = if (role == WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader) {
                          Seq(name, email, contactNumber, companyName)
                        } else {
                          Seq(name, email, contactNumber)
                        }
    } yield result

  private def getMessageKey(role: WhatIsYourRoleAsImporter, fieldName: String): String =
    role match {
      case WhatIsYourRoleAsImporter.EmployeeOfOrg         =>
        s"checkYourAnswersForAgents.applicant.$fieldName.label"
      case WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg    =>
        s"checkYourAnswersForAgents.agent.org.$fieldName.label"
      case WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader =>
        s"agentForTraderCheckYourAnswers.applicant.$fieldName.label"
    }

  private def getAriaMessageKey(role: WhatIsYourRoleAsImporter, fieldName: String): String =
    role match {
      case WhatIsYourRoleAsImporter.EmployeeOfOrg         =>
        s"checkYourAnswersForAgents.applicant.$fieldName.change.hidden"
      case WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg    =>
        s"checkYourAnswersForAgents.agent.org.$fieldName.change.hidden"
      case WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader =>
        s"agentForTraderCheckYourAnswers.applicant.$fieldName.hidden"
    }
}
