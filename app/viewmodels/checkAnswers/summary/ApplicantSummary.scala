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

package viewmodels.checkAnswers.summary

import cats.syntax.all._

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import models.UserAnswers
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

sealed trait ApplicantSummary {
  def rows: SummaryList
}

case class IndividualApplicantSummary(rows: SummaryList) extends ApplicantSummary

class IndividualApplicantSummaryCreator {
  def summaryRows(
    userAnswers: UserAnswers
  )(implicit messages: Messages): IndividualApplicantSummary = {
    val contactDetailsRows = ApplicationContactDetailsSummary.rows(userAnswers).orEmpty
    IndividualApplicantSummary(SummaryListViewModel(contactDetailsRows))
  }
}

case class AgentSummary(rows: SummaryList) extends ApplicantSummary

class AgentSummaryCreator {
  def summaryRows(userAnswers: UserAnswers)(implicit messages: Messages): AgentSummary = {
    val roleRow            = AgentRoleSummary.row(userAnswers, userAnswers.draftId).orEmpty
    val contactDetailsRows = BusinessContactDetailsSummary.rows(userAnswers).orEmpty
    val agentCompanyRow    = AgentCompanySummary.rows(userAnswers).orEmpty

    AgentSummary(SummaryListViewModel(roleRow ++ contactDetailsRows ++ agentCompanyRow))
  }
}
