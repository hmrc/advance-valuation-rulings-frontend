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
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models.{CheckMode, Country, DraftId, EoriNumber, TraderDetailsWithCountryCode}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AgentForTraderCheckRegisteredDetailsSummary {

  private def registeredNameRow(details: TraderDetailsWithCountryCode, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "agentForTraderCheckYourAnswers.trader.name.label",
      value = ValueViewModel(HtmlFormat.escape(details.CDSFullName).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("agentForTraderCheckYourAnswers.trader.name.hidden"))
      )
    )

  private def registeredAddressRow(details: TraderDetailsWithCountryCode, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "agentForTraderCheckYourAnswers.trader.address.label",
      value = ValueViewModel(
        HtmlContent(
          Html(
            s"${HtmlFormat.escape(details.CDSEstablishmentAddress.streetAndNumber).body}<br>" +
              s"${HtmlFormat.escape(details.CDSEstablishmentAddress.city).body}<br>" +
              details.CDSEstablishmentAddress.postalCode
                .map(value => s"${HtmlFormat.escape(value).body}<br>")
                .getOrElse("") +
              HtmlFormat.escape(
                Country.fromCountryCode(details.CDSEstablishmentAddress.countryCode).name
              )
          )
        )
      ),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("agentForTraderCheckYourAnswers.trader.address.hidden"))
      )
    )

  private def registeredNumberRow(eoriNumber: EoriNumber, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "agentForTraderCheckYourAnswers.trader.eori.number.label",
      value = ValueViewModel(HtmlFormat.escape(eoriNumber.value).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(
            messages("agentForTraderCheckYourAnswers.trader.eori.number.hidden")
          )
      )
    )

  private def letterOfAuthorityRow(draftId: DraftId, letterOfAuthorityFileName: String)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "agentForTraderCheckYourAnswers.trader.loa.label",
      value = ValueViewModel(HtmlFormat.escape(letterOfAuthorityFileName).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.UploadLetterOfAuthorityController.onPageLoad(CheckMode, draftId, None, None).url
        )
          .withVisuallyHiddenText(
            messages("agentForTraderCheckYourAnswers.trader.loa.hidden")
          )
      )
    )

  def rows(
    details: TraderDetailsWithCountryCode,
    draftId: DraftId,
    letterOfAuthorityFileName: String
  )(implicit
    messages: Messages
  ): Option[Seq[SummaryListRow]] = {
    val number          = registeredNumberRow(EoriNumber(details.EORINo), draftId)
    val personalDetails = if (details.consentToDisclosureOfPersonalData) {
      val name              = registeredNameRow(details, draftId)
      val address           = registeredAddressRow(details, draftId)
      val letterOfAuthority = letterOfAuthorityRow(draftId, letterOfAuthorityFileName)
      Seq(name, address, letterOfAuthority)
    } else {
      Nil
    }
    Some(number +: personalDetails)
  }
}
