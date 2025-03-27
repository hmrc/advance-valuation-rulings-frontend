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
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryList, SummaryListRow}
import models.requests.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import viewmodels.application.*
import viewmodels.checkAnswers.*
import viewmodels.govuk.summarylist.*

case class ApplicationViewModel(
  eori: SummaryList,
  trader: Option[SummaryList],
  agent: Option[SummaryList],
  details: SummaryList,
  method: SummaryList
)

object ApplicationViewModel {
  def apply(application: Application)(implicit
    messages: Messages
  ): ApplicationViewModel = {
    val eori                    = getEoriRow(application)
    val dateSubmitted           = DateSubmittedSummary.row(application)
    val roleDescription         = RoleDetailsSummary
      .rowRoleDescription(application.whatIsYourRoleResponse)
      .getOrElse(SummaryListRowViewModel(Key(), Value()))
    val agentTraderDetails      = AgentTraderDetailsSummary.rowsAgentDetails(application.contact)
    val agentCompanyRows        = application.agent.map(agent => AgentDetailsSummary.rows(agent)).getOrElse(Nil)
    val goodsDetails            = GoodsDetailsSummary.rows(application.goodsDetails, application.attachments)
    val methodDetails           = RequestedMethodSummary.rows(application.requestedMethod)
    val applicant               = ContactDetailsSummary.rows(application.contact)
    val letterOfAuthorityOption = AgentTraderDetailsSummary
      .rowLetterOfAuthority(application.letterOfAuthority)
      .getOrElse(SummaryListRowViewModel(Key(), Value()))

    val eoriRow =
      val traderDetails = application.trader.isPrivate
        .filter(isPrivate => !isPrivate)
        .map(_ => AgentTraderDetailsSummary.rowsTraderDetails(application.trader))
        .toList
        .flatten

      applicationRole(application) match {
        case "applicant" => eori ++ applicant :+ dateSubmitted
        case "trader"    => eori ++ traderDetails :+ letterOfAuthorityOption
        case _           => eori
      }

    val agentDetails: Seq[SummaryListRow] =
      if (applicationRole(application).equals("trader")) {
        agentTraderDetails :+ dateSubmitted
      } else {
        agentTraderDetails ++ agentCompanyRows :+ dateSubmitted
      }

    applicationRole(application) match {
      case "trader"       =>
        ApplicationViewModel(
          eori = SummaryList(eoriRow),
          trader = Some(SummaryList(roleDescription +: agentDetails)),
          agent = None,
          details = SummaryList(goodsDetails),
          method = SummaryList(methodDetails)
        )
      case "organisation" =>
        ApplicationViewModel(
          eori = SummaryList(eoriRow),
          trader = None,
          agent = Some(SummaryList(roleDescription +: agentDetails)),
          details = SummaryList(goodsDetails),
          method = SummaryList(methodDetails)
        )
      case _              =>
        ApplicationViewModel(
          eori = SummaryList(roleDescription +: eoriRow),
          trader = None,
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
      Seq(AgentTraderDetailsSummary.rowTraderEori(application.trader.eori))
    } else {
      RegisteredDetailsSummary.rows(application.trader)
    }

  private def applicationRole(application: Application) =
    application.whatIsYourRoleResponse match {
      case Some(WhatIsYourRole.AgentTrader) => "trader"
      case Some(WhatIsYourRole.AgentOrg)    => "organisation"
      case _                                => "applicant"
    }
}
