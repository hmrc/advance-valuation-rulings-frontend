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

package viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import models.requests.*
import viewmodels.application.*
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist._

case class ApplicationViewModel(
  eori: SummaryList,
  applicant: Option[SummaryList],
  agent: Option[SummaryList],
  details: SummaryList,
  method: SummaryList
)

object ApplicationViewModel {
  def apply(application: Application)(implicit
                                      messages: Messages
  ): ApplicationViewModel = {
    val eoriRow         = getEoriRow(application)
    val dateSubmitted   = DateSubmittedSummary.row(application)
    val roleDescription = RoleDetailsSummary.rowRoleDescription(application.whatIsYourRoleResponse)
    val agentCompanyRows= application.agent.map(agent => AgentDetailsSummary.rows(agent)).getOrElse(Nil)
    val agentDetails    = roleDescription.get +: AgentTraderDetailsSummary.rowsAgentDetails(application.contact)
    val goodsDetails    = GoodsDetailsSummary.rows(application.goodsDetails, application.attachments)
    val methodDetails   = RequestedMethodSummary.rows(application.requestedMethod)
    val applicant       = ContactDetailsSummary.rows(application.contact) :+ dateSubmitted
    val letterOfAuthorityOption =
      AgentTraderDetailsSummary.rowLetterOfAuthority(application.letterOfAuthority)

    val agent: Seq[SummaryListRow] =
      if (applicationRole(application).equals("trader")) {
        val traderDetails           = application.trader.isPrivate
          .filter(isPrivate => !isPrivate)
          .map(_ => AgentTraderDetailsSummary.rowsTraderDetails(application.trader))
          .toList
          .flatten

        traderDetails ++ letterOfAuthorityOption
      } else {
        agentDetails ++ agentCompanyRows :+ dateSubmitted
      }

    applicationRole(application) match {
      case "trader" =>
      ApplicationViewModel(
        eori = SummaryList(eoriRow ++ letterOfAuthorityOption),
        applicant = Some(SummaryList(agent)),
        agent = Some(SummaryList(agentDetails :+ dateSubmitted)),
        details = SummaryList(goodsDetails),
        method = SummaryList(methodDetails)
      )
      case "organisation" =>   ApplicationViewModel(
        eori = SummaryList(eoriRow),
        applicant = None,
        agent = Some(SummaryList(agent)),
        details = SummaryList(goodsDetails),
        method = SummaryList(methodDetails)
      )
      case _ =>   ApplicationViewModel(
        eori = SummaryList(roleDescription.get +: eoriRow),
        applicant = Some(SummaryList(applicant)),
        agent = None,
        details = SummaryList(goodsDetails),
        method = SummaryList(methodDetails)
      )
    }

  }

  private def getEoriRow(application: Application)(implicit
                                                   messages: Messages
  ): Seq[SummaryListRow] =
    if (applicationRole(application).equals("trader")) {
      AgentTraderDetailsSummary.rowTraderDetailsWithEori(application.trader)
    } else {
      RegisteredDetailsSummary.rows(application.trader)
    }


  private def applicationRole(application: Application) = {
    application.whatIsYourRoleResponse match {
      case Some(WhatIsYourRole.AgentTrader) => "trader"
      case Some(WhatIsYourRole.AgentOrg) => "organisation"
      case _ => "applicant"
    }
  }
}

