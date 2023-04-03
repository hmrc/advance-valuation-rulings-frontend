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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import models.requests._
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.summary._

case class ApplicationViewModel(
  eori: SummaryList,
  applicant: SummaryList,
  details: DetailsSummary,
  method: MethodSummary
)

object ApplicationViewModel {
  def apply(application: Application)(implicit
    messages: Messages
  ): ApplicationViewModel = {

    val appRequest = application.request

    val applicantRows = appRequest.applicant match {
      case IndividualApplicant(_)         =>
        val dateSubmitted = DateSubmittedSummary.row(application)
        val contact       = ApplicationContactDetailsSummary.rows(appRequest).map(_.copy(actions = None))
        contact :+ dateSubmitted
      case OrganisationApplicant(_, role) =>
        val contact       = BusinessContactDetailsSummary.rows(appRequest).map(_.copy(actions = None))
        val agentRole     = AgentRoleSummary.row(role).copy(actions = None)
        val dateSubmitted = DateSubmittedSummary.row(application)

        contact :+ agentRole :+ dateSubmitted
    }

    val eoriRow = appRequest.applicant match {
      case IndividualApplicant(_)      =>
        CheckRegisteredDetailsSummary.rows(appRequest).map(_.copy(actions = None))
      case OrganisationApplicant(_, _) =>
        CheckRegisteredDetailsForAgentsSummary.rows(appRequest).map(_.copy(actions = None))
    }

    ApplicationViewModel(
      eori = SummaryList(eoriRow),
      applicant = SummaryList(applicantRows),
      details = DetailsSummary(appRequest).removeActions(),
      method = MethodSummary(appRequest).removeActions()
    )
  }
}
