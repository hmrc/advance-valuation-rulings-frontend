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

package viewmodels.application

import models.requests.TraderDetail
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

class RegisteredDetailsSummarySpec extends AnyFreeSpec with Matchers {

  private given m: Messages = stubMessages()

  private val trader = TraderDetail(
    eori = "eori",
    businessName = "business name",
    addressLine1 = "address line 1",
    addressLine2 = Some("address line 2"),
    addressLine3 = None,
    postcode = "postcode",
    countryCode = "country code",
    phoneNumber = None,
    isPrivate = Some(false)
  )

  ".rows" - {

    "must contain a row for the trader's EORI" in {

      RegisteredDetailsSummary.rows(trader) must contain only SummaryListRow(
        Key(Text(m("checkYourAnswers.eori.number.label"))),
        Value(Text(trader.eori))
      )
    }
  }
}
