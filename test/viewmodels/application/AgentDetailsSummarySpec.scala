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

package viewmodels.application

import models.requests.TraderDetail
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

class AgentDetailsSummarySpec extends AnyFreeSpec with Matchers {

  private implicit val m: Messages = stubMessages()

  ".rows" - {

    "must contain rows for eori, business name and address" in {

      val agent = TraderDetail(
        eori = "eori",
        businessName = "name",
        addressLine1 = "line 1",
        addressLine2 = Some("line 2"),
        addressLine3 = None,
        postcode = "AA1 1AA",
        countryCode = "GB",
        phoneNumber = None,
        isPrivate = None
      )

      AgentDetailsSummary.rows(agent) must contain theSameElementsInOrderAs Seq(
        SummaryListRow(
          Key(Text(m("checkYourAnswersForAgents.agent.eori.number.label"))),
          Value(Text(agent.eori))
        ),
        SummaryListRow(
          Key(Text(m("checkYourAnswersForAgents.agent.name.label"))),
          Value(Text(agent.businessName))
        ),
        SummaryListRow(
          Key(Text(m("checkYourAnswersForAgents.agent.address.label"))),
          Value(HtmlContent(Html("line 1<br/>line 2<br/>AA1 1AA<br/>United Kingdom")))
        )
      )
    }
  }
}
