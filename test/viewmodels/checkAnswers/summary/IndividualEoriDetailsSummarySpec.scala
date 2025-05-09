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

import base.SpecBase
import models.{CDSEstablishmentAddress, DraftId, TraderDetailsWithCountryCode}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.Key

class IndividualEoriDetailsSummarySpec extends SpecBase {

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

  "IndividualEoriDetailsSummary" - {
    given m: Messages = play.api.test.Helpers.stubMessages()

    "when the user has answers for all relevant pages" - {
      val summary = new IndividualEoriDetailsSummaryCreator().summaryRows(
        registeredDetails,
        DraftId(0),
        emptyUserAnswers
      )
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "must create rows for each page" in {
        rows.length mustBe 3
      }

      "create row for EORI number" in {
        rows must contain(
          (
            Key(Text("checkYourAnswers.eori.number.label")),
            Value(Text(EoriNumber))
          )
        )
      }

      "create row for EORI registered name" in {
        rows must contain(
          (
            Key(Text("checkYourAnswers.eori.name.label")),
            Value(Text(RegisteredName))
          )
        )
      }

      "create row for EORI registered address" in {
        rows must contain(
          (
            Key(Text("checkYourAnswers.eori.address.label")),
            Value(HtmlContent(s"$StreetAndNumber<br>$City<br>$Postcode<br>$countryAsString"))
          )
        )
      }
    }

    "when consentToDisclosureOfPersonalData is false" - {
      val summary = new IndividualEoriDetailsSummaryCreator().summaryRows(
        registeredDetails.copy(consentToDisclosureOfPersonalData = false),
        DraftId(0),
        emptyUserAnswers
      )
      val rows    = summary.rows.rows.map(row => (row.key, row.value))

      "create only EORI number row" in {
        rows must contain theSameElementsAs Seq(
          (
            Key(Text("checkYourAnswers.eori.number.label")),
            Value(Text(EoriNumber))
          )
        )
      }
    }
  }
}
