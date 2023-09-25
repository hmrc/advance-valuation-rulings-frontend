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

import java.time.{Clock, Instant, ZoneOffset}

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

import base.SpecBase
import generators.Generators
import models._
import models.ValuationMethod.Method3
import models.requests._

class ApplicationViewModelSpec extends SpecBase {
  import ApplicationViewModelSpec._

  "ApplicationViewModel should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given a valid application for a trader" - {

      val result = ApplicationViewModel(application)

      "must create rows for the eori details" in {
        result.eori.rows must be(
          Seq(
            SummaryListRow(
              Key(Text("checkYourAnswers.eori.number.label")),
              Value(Text(eoriDetails.eori))
            )
          )
        )
      }

      "must create rows for the applicant" - {

        "must create 4 rows" in {
          result.applicant.rows.length must be(4)
        }
        "must create row for the applicant name" in {
          result.applicant.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.name.label")),
              Value(Text(contact.name))
            )
          )
        }
        "must create row for the applicant email" in {
          result.applicant.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.email.label")),
              Value(Text(contact.email))
            )
          )
        }
        "must create row for the applicant phone" in {
          result.applicant.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.phone.label")),
              Value(Text(contact.phone.get))
            )
          )
        }
        "must create row for date submitted" in {
          result.applicant.rows must contain(
            SummaryListRow(
              Key(Text("viewApplication.dateSubmitted")),
              Value(Text("22 August 2018"))
            )
          )
        }
      }

      "must create rows for the goods" in {
        result.details.rows mustBe Seq(
          SummaryListRow(
            Key(Text("descriptionOfGoods.checkYourAnswersLabel")),
            Value(Text(goodsDetails.goodsName))
          ),
          SummaryListRow(
            Key(Text("commodityCode.checkYourAnswersLabel")),
            Value(Text(goodsDetails.envisagedCommodityCode.get))
          ),
          SummaryListRow(
            Key(Text("describeTheLegalChallenges.checkYourAnswersLabel")),
            Value(Text(goodsDetails.knownLegalProceedings.get))
          ),
          SummaryListRow(
            Key(Text("confidentialInformation.checkYourAnswersLabel")),
            Value(Text(goodsDetails.confidentialInformation.get))
          )
        )
      }

      "must create rows for the method" in {
        result.method.rows mustBe Seq(
          SummaryListRow(
            Key(Text("valuationMethod.checkYourAnswersLabel")),
            Value(Text(s"valuationMethod.${Method3.toString}"))
          ),
          SummaryListRow(
            Key(Text("whyTransactionValueOfSimilarGoods.checkYourAnswersLabel")),
            Value(Text(requestedMethod.whyNotOtherMethods))
          ),
          SummaryListRow(
            Key(Text("haveYouUsedMethodOneForSimilarGoodsInPast.checkYourAnswersLabel")),
            Value(Text("site.yes"))
          ),
          SummaryListRow(
            Key(Text("describeTheSimilarGoods.checkYourAnswersLabel")),
            Value(Text(requestedMethod.previousSimilarGoods.value))
          )
        )
      }
    }

    "when given a valid application with agent details" - {
      val agent            = TraderDetail(
        eori = "agent eori",
        businessName = "agent business name",
        addressLine1 = "agent address line 1",
        addressLine2 = Some("AgentCity"),
        addressLine3 = None,
        postcode = "postcode",
        countryCode = "GB",
        phoneNumber = None
      )
      val agentApplication = application.copy(agent = Some(agent))
      val result           = ApplicationViewModel(agentApplication)

      "must create 7 rows" in {
        result.applicant.rows.length must be(7)
      }
      "must create row for the applicant name" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswers.applicant.name.label")),
            Value(Text(contact.name))
          )
        )
      }
      "must create row for the applicant email" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswers.applicant.email.label")),
            Value(Text(contact.email))
          )
        )
      }
      "must create row for the applicant phone" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswers.applicant.phone.label")),
            Value(Text(contact.phone.get))
          )
        )
      }
      "must create row for the agent eori" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswersForAgents.agent.eori.number.label")),
            Value(Text(agent.eori))
          )
        )
      }
      "must create row for the agent business name" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswersForAgents.agent.name.label")),
            Value(Text(agent.businessName))
          )
        )
      }
      "must create row for the agent address" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswersForAgents.agent.address.label")),
            Value(
              HtmlContent(
                "agent address line 1<br/>AgentCity<br/>postcode<br/>United Kingdom"
              )
            )
          )
        )
      }
      "must create row for date submitted" in {
        result.applicant.rows must contain(
          SummaryListRow(
            Key(Text("viewApplication.dateSubmitted")),
            Value(Text("22 August 2018"))
          )
        )
      }
    }
  }
}

object ApplicationViewModelSpec extends Generators {

  val eoriDetails: TraderDetail = TraderDetail(
    eori = "eori",
    businessName = "business name",
    addressLine1 = "address line 1",
    addressLine2 = Some("address line 2"),
    addressLine3 = None,
    postcode = "postcode",
    countryCode = "country code",
    phoneNumber = None
  )

  val contact: ContactDetails = ContactDetails(
    name = "contact name",
    email = "email@example.com",
    phone = Some("phone")
  )

  val requestedMethod: MethodThree = MethodThree(
    whyNotOtherMethods = "method 3 why not",
    previousSimilarGoods = PreviousSimilarGoods("previous similar goods")
  )

  val goodsDetails: GoodsDetails = GoodsDetails(
    goodsName = "goods name",
    goodsDescription = "goods description",
    envisagedCommodityCode = Some("commodity code"),
    knownLegalProceedings = Some("legal"),
    confidentialInformation = Some("confidential")
  )

  val lastUpdated: Instant                   =
    Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC))
  val lastUpdatedString: String              = "22/08/2018"
  val draftId: DraftId                       = DraftId(0)
  val applicationRequest: ApplicationRequest = ApplicationRequest(
    draftId = draftId,
    trader = eoriDetails,
    agent = None,
    contact = contact,
    requestedMethod = requestedMethod,
    goodsDetails = goodsDetails,
    attachments = Nil,
    whatIsYourRole = WhatIsYourRole.EmployeeOrg,
    letterOfAuthority = None
  )
  val applicationId: ApplicationId           = ApplicationId(0L)
  val application: Application =
    Application(
      id = applicationId,
      lastUpdated = lastUpdated,
      created = lastUpdated,
      trader = applicationRequest.trader,
      agent = applicationRequest.agent,
      contact = applicationRequest.contact,
      requestedMethod = applicationRequest.requestedMethod,
      goodsDetails = applicationRequest.goodsDetails,
      attachments = Nil,
      whatIsYourRoleResponse = Some(WhatIsYourRole.EmployeeOrg)
    )
}
