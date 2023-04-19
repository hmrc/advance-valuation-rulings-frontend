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

package viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import models.requests._
import viewmodels.application.{ContactDetailsSummary, GoodsDetailsSummary, RegisteredDetailsSummary, RequestedMethodSummary}
import viewmodels.checkAnswers._

case class ApplicationViewModel(
  eori: SummaryList,
  applicant: SummaryList,
  details: SummaryList,
  method: SummaryList
)

object ApplicationViewModel {
  def apply(application: Application)(implicit
    messages: Messages
  ): ApplicationViewModel = {
    val eoriRow       = RegisteredDetailsSummary.rows(application.trader)
    val agentRows     = AgentCompanySummary.rows(application.agent)
    val applicant     = ContactDetailsSummary.rows(application.contact)
    val dateSubmitted = DateSubmittedSummary.row(application)
    val goodsDetails  = GoodsDetailsSummary.rows(application.goodsDetails, application.attachments)
    val methodDetails = RequestedMethodSummary.rows(application.requestedMethod)

    ApplicationViewModel(
      eori = SummaryList(eoriRow),
      applicant = SummaryList(applicant :++ agentRows :+ dateSubmitted),
      details = SummaryList(goodsDetails),
      method = SummaryList(methodDetails)
    )
  }
}
