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
import models.{BusinessContactDetails, CheckMode, DraftId, UserAnswers}
import pages.BusinessContactDetailsPage
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object BusinessContactDetailsSummary {

  private def nameRow(answer: BusinessContactDetails, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.applicant.name.label",
      value = ValueViewModel(HtmlFormat.escape(answer.name).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("businessContactDetails.name.change.hidden"))
      )
    )

  private def emailRow(answer: BusinessContactDetails, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.applicant.email.label",
      value = ValueViewModel(HtmlFormat.escape(answer.email).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("businessContactDetails.email.change.hidden"))
      )
    )

  private def contactNumberRow(answer: BusinessContactDetails, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswersForAgents.applicant.phone.label",
      value = ValueViewModel(HtmlFormat.escape(answer.phone).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("businessContactDetails.phone.change.hidden"))
      )
    )

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(BusinessContactDetailsPage)
      name            = nameRow(contactDetails, userAnswer.draftId)
      email           = emailRow(contactDetails, userAnswer.draftId)
      contactNumber   = contactNumberRow(contactDetails, userAnswer.draftId)
      result          = Seq(name, email, contactNumber)
    } yield result
}
