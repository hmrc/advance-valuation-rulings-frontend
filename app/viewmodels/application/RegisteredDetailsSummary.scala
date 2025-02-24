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

package viewmodels.application

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import models.requests.TraderDetail
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object RegisteredDetailsSummary {

  def rows(trader: TraderDetail)(implicit messages: Messages): Seq[SummaryListRow] = {
    val addressLines = Seq(
      Some(trader.addressLine1),
      trader.addressLine2,
      trader.addressLine3,
      Some(trader.postcode),
      Some(trader.countryCode)
    ).flatten.mkString("<br/>")

    Seq(
      SummaryListRowViewModel(
        key = "checkYourAnswers.eori.number.label",
        value = ValueViewModel(trader.eori)
      ),
      SummaryListRowViewModel(
        key = "checkYourAnswers.eori.name.label",
        value = ValueViewModel(trader.businessName)
      ),
      SummaryListRowViewModel(
        key = "checkYourAnswers.eori.address.label",
        value = ValueViewModel(HtmlContent(Html(addressLines)))
      )
    )
  }

}
