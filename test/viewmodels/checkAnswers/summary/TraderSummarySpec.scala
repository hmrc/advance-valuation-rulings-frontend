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

import base.SpecBase
import generators.Generators
import models.{CDSEstablishmentAddress, DraftId, TraderDetailsWithCountryCode}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

class TraderSummarySpec extends SpecBase with Generators {

  private val registeredDetails: TraderDetailsWithCountryCode = TraderDetailsWithCountryCode(
    EORINo = EoriNumber,
    consentToDisclosureOfPersonalData = true,
    CDSFullName = RegisteredName,
    CDSEstablishmentAddress = CDSEstablishmentAddress(
      streetAndNumber = StreetAndNumber,
      city = City,
      countryCode = countryCode,
      postalCode = Some(Postcode)
    ),
    contactInformation = None
  )
  private val letterOfAuthorityFileName                       = "some file name.png"

  "AgentForTraderCheckRegisteredDetailsSummary" - {
    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when the user has answers for all relevant pages" - {
      val summary =
        new TraderEoriDetailsSummaryCreator().summaryRows(
          registeredDetails,
          DraftId(0),
          letterOfAuthorityFileName
        )
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "must create rows for each page" in {
        rows.length mustBe 4
      }

      "create row for business EORI number" in {
        rows must contain(
          (
            Key(Text("agentForTraderCheckYourAnswers.trader.eori.number.label")),
            Value(Text(EoriNumber))
          )
        )
      }

      "create row for business EORI registered name" in {
        rows must contain(
          (
            Key(Text("agentForTraderCheckYourAnswers.trader.name.label")),
            Value(Text(RegisteredName))
          )
        )
      }

      "create row for business EORI registered address" in {
        rows must contain(
          (
            Key(Text("agentForTraderCheckYourAnswers.trader.address.label")),
            Value(HtmlContent(s"$StreetAndNumber<br>$City<br>$Postcode<br>$countryAsString"))
          )
        )
      }

      "create row for trader letter of authority" in {
        rows must contain(
          (
            Key(Text("agentForTraderCheckYourAnswers.trader.loa.label")),
            Value(Text(letterOfAuthorityFileName))
          )
        )
      }

    }

    "when consentToDisclosureOfPersonalData is false" - {
      val details = registeredDetails.copy(consentToDisclosureOfPersonalData = false)
      val summary = new TraderEoriDetailsSummaryCreator().summaryRows(
        details,
        DraftId(0),
        letterOfAuthorityFileName
      )
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "must create rows for each page" in {
        rows.length mustBe 2
      }

      "create row for business EORI number" in {
        rows must contain(
          (
            Key(Text("agentForTraderCheckYourAnswers.trader.eori.number.label")),
            Value(Text(EoriNumber))
          )
        )
      }

      "create row for trader letter of authority" in {
        rows must contain(
          (
            Key(Text("agentForTraderCheckYourAnswers.trader.loa.label")),
            Value(Text(letterOfAuthorityFileName))
          )
        )
      }

    }
  }
}
