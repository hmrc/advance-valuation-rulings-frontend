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

package viewmodels.checkAnswers.summary

import cats.syntax.all._

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import models.{DraftId, TraderDetailsWithCountryCode, UserAnswers}
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

sealed trait EoriDetailsSummary {
  def rows: SummaryList
}

case class IndividualEoriDetailsSummary(rows: SummaryList) extends EoriDetailsSummary

class IndividualEoriDetailsSummaryCreator {
  def summaryRows(
    details: TraderDetailsWithCountryCode,
    draftId: DraftId,
    userAnswers: UserAnswers
  )(implicit
    messages: Messages
  ): EoriDetailsSummary = {
    val roleRow = AgentRoleSummary.row(userAnswers, draftId).orEmpty
    val rows    = CheckRegisteredDetailsSummary.rows(details, draftId).orEmpty
    IndividualEoriDetailsSummary(SummaryListViewModel(roleRow ++ rows))
  }
}

case class BusinessEoriDetailsSummary(rows: SummaryList) extends EoriDetailsSummary

class BusinessEoriDetailsSummaryCreator {
  def summaryRows(details: TraderDetailsWithCountryCode, draftId: DraftId)(implicit
    messages: Messages
  ): EoriDetailsSummary = {

    val rows = CheckRegisteredDetailsForAgentsSummary.rows(details, draftId).orEmpty
    BusinessEoriDetailsSummary(SummaryListViewModel(rows))
  }
}

case class TraderEoriDetailsSummary(rows: SummaryList) extends EoriDetailsSummary

class TraderEoriDetailsSummaryCreator {
  def summaryRows(
    details: TraderDetailsWithCountryCode,
    draftId: DraftId,
    letterOfAuthorityFileName: String
  )(implicit
    messages: Messages
  ): EoriDetailsSummary = {

    val rows = AgentForTraderCheckRegisteredDetailsSummary
      .rows(details, draftId, letterOfAuthorityFileName)
      .orEmpty
    TraderEoriDetailsSummary(SummaryListViewModel(rows))
  }
}
