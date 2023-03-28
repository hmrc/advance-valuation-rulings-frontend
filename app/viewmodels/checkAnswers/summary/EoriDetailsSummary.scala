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

package viewmodels.checkAnswers.summary

import cats.syntax.all._

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import models.UserAnswers
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

sealed trait EoriDetailsSummary {
  def removeActions(): EoriDetailsSummary
  def rows: SummaryList
}

case class IndividualEoriDetailsSummary(rows: SummaryList) extends EoriDetailsSummary {
  def removeActions(): EoriDetailsSummary = IndividualEoriDetailsSummary(
    SummaryListViewModel(rows.rows.map(_.copy(actions = None)))
  )
}

object IndividualEoriDetailsSummary {
  def apply(userAnswers: UserAnswers)(implicit messages: Messages): EoriDetailsSummary = {

    val rows = CheckRegisteredDetailsSummary.rows(userAnswers).orEmpty
    IndividualEoriDetailsSummary(SummaryListViewModel(rows))
  }
}

case class BusinessEoriDetailsSummary(rows: SummaryList) extends EoriDetailsSummary {
  def removeActions(): EoriDetailsSummary = BusinessEoriDetailsSummary(
    SummaryListViewModel(rows.rows.map(_.copy(actions = None)))
  )
}

object BusinessEoriDetailsSummary {
  def apply(userAnswers: UserAnswers)(implicit messages: Messages): EoriDetailsSummary = {

    val rows = CheckRegisteredDetailsForAgentsSummary.rows(userAnswers).orEmpty
    BusinessEoriDetailsSummary(SummaryListViewModel(rows))
  }
}
