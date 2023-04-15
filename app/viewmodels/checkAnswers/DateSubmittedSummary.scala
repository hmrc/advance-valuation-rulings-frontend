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

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import models.requests._

object DateSubmittedSummary {
  import viewmodels.govuk.summarylist._
  import viewmodels.implicits._

  private val formatter = DateTimeFormatter
    .ofPattern("dd MMMM yyyy")
    .withZone(ZoneId.systemDefault())
    .withLocale(Locale.UK)

  def row(application: Application)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = "viewApplication.dateSubmitted",
      value = ValueViewModel(formatter.format(application.created))
    )
}
