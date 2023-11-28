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

import models.requests.{Attachment, ContactDetails, Privacy, TraderDetail}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

class AgentTraderDetailsSummarySpec extends AnyFreeSpec with Matchers {

  private implicit val m: Messages = stubMessages()

  private val eori: String                  = "EORI123"
  val letterOfAuthority: Option[Attachment] = Some(
    Attachment(
      45L,
      "letter of authority",
      None,
      "",
      Privacy.Public,
      "",
      3L
    )
  )

  val trader: TraderDetail = TraderDetail(
    eori,
    "Business Name",
    "Address Line 1",
    Some("Address Line 2"),
    Some("Address Line 3"),
    "NE1 4FF",
    "country code",
    Some("0123456789"),
    Some(true)
  )

  val minTraderDetail: TraderDetail = TraderDetail(
    eori,
    "Business Name",
    "Address Line 1",
    None,
    None,
    "NE1 4FF",
    "country code",
    None,
    Some(false)
  )

  val maxContactDetails: ContactDetails = ContactDetails(
    "Joe Blogs",
    "jb@something.com",
    Some("9876543210"),
    Some("Company Name"),
    Some("CEO")
  )

  val minContactDetails: ContactDetails = ContactDetails(
    "Joe Blogs",
    "jb@something.com",
    None,
    None,
    None
  )

  "Trader EORI row" - {
    "must return the trader EORI" in {
      AgentTraderDetailsSummary.rowTraderEori(eori) mustEqual SummaryListRow(
        Key(Text(m("agentForTraderCheckYourAnswers.trader.eori.number.label"))),
        Value(Text(eori))
      )
    }
  }

  "Letter of authority row" - {

    "must return the letter of authority if it exists" in {
      AgentTraderDetailsSummary
        .rowLetterOfAuthority(letterOfAuthority)
        .get mustEqual SummaryListRow(
        Key(Text(m("agentForTraderCheckYourAnswers.trader.loa.label"))),
        Value(Text(letterOfAuthority.get.name))
      )
    }

    "must return the None if it does not exist" in {
      AgentTraderDetailsSummary.rowLetterOfAuthority(None) must be(None)
    }
  }

  "Trader details row" - {
    val allAddressLines = Seq(
      Some(trader.addressLine1),
      trader.addressLine2,
      trader.addressLine3,
      Some(trader.postcode),
      Some(trader.countryCode)
    ).flatten.mkString("<br/>")

    val minAddressLines = Seq(
      Some(minTraderDetail.addressLine1),
      Some(minTraderDetail.postcode),
      Some(minTraderDetail.countryCode)
    ).flatten.mkString("<br/>")

    "must return all trader details" in {
      AgentTraderDetailsSummary.rowsTraderDetails(trader) must contain theSameElementsAs Seq(
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.trader.name.label"))),
          Value(Text(trader.businessName))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.trader.address.label"))),
          Value(HtmlContent(Html(allAddressLines)))
        )
      )
    }

    "must return minimum trader details" in {
      AgentTraderDetailsSummary.rowsTraderDetails(
        minTraderDetail
      ) must contain theSameElementsAs Seq(
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.trader.name.label"))),
          Value(Text(minTraderDetail.businessName))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.trader.address.label"))),
          Value(HtmlContent(Html(minAddressLines)))
        )
      )
    }
  }

  "Agent details row" - {
    "must return all agent details" in {
      AgentTraderDetailsSummary.rowsAgentDetails(
        maxContactDetails
      ) must contain theSameElementsAs Seq(
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.name.label"))),
          Value(Text(maxContactDetails.name))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.email.label"))),
          Value(Text(maxContactDetails.email))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.phone.label"))),
          Value(Text(maxContactDetails.phone.get))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.companyName.label"))),
          Value(Text(maxContactDetails.companyName.get))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.jobTitle.label"))),
          Value(Text(maxContactDetails.jobTitle.get))
        )
      )
    }

    "must return minimum agent details" in {
      AgentTraderDetailsSummary.rowsAgentDetails(
        minContactDetails
      ) must contain theSameElementsAs Seq(
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.name.label"))),
          Value(Text(minContactDetails.name))
        ),
        SummaryListRow(
          Key(Text(m("agentForTraderCheckYourAnswers.applicant.email.label"))),
          Value(Text(minContactDetails.email))
        )
      )
    }
  }

}
