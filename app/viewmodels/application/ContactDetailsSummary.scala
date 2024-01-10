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

package viewmodels.application

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import models.requests.ContactDetails
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ContactDetailsSummary extends ApplicationSummaryHelper {

  def rows(contact: ContactDetails)(implicit messages: Messages): Seq[SummaryListRow] = {
    val mandatoryRows = Seq(
      SummaryListRowViewModel(
        key = "checkYourAnswers.applicant.name.label",
        value = ValueViewModel(contact.name)
      ),
      SummaryListRowViewModel(
        key = "checkYourAnswers.applicant.email.label",
        value = ValueViewModel(contact.email)
      )
    )

    val phoneRow = makeRowFromOption(
      key = "checkYourAnswers.applicant.phone.label",
      field = contact.phone
    )

    val jobTitleRow = makeRowFromOption(
      key = "checkYourAnswers.applicant.jobTitle.label",
      field = contact.jobTitle
    )

    mandatoryRows ++ phoneRow ++ jobTitleRow
  }
}
