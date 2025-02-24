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

import base.SpecBase
import generators.Generators
import models.ValuationMethod.Method3
import models._
import models.requests._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{HtmlContent, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

import java.time.{Clock, Instant, ZoneOffset}

class ApplicationViewModelSpec extends SpecBase {
  import ApplicationViewModelSpec._

  "ApplicationViewModel should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given a valid application for an employee" - {

      val result = ApplicationViewModel(application)

      "must create rows for the employee" - {

        "must create 9 rows" in {
          result.eori.rows.length must be(9)
        }

        "must create row for the role description" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswersForAgents.applicant.role.label")),
              Value(Text("whatIsYourRoleAsImporter.employeeOfOrg"))
            )
          )
        }

        "must create row for the organisation's eori" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.eori.number.label")),
              Value(Text(eoriDetails.eori))
            )
          )
        }

        "must create row for the organisation's business name" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.eori.name.label")),
              Value(Text(eoriDetails.businessName))
            )
          )
        }

        "must create row for the organisation's business address" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.eori.address.label")),
              Value(
                HtmlContent(
                  eoriDetails.addressLine1 + "<br/>" + eoriDetails.addressLine2.get + "<br/>" + eoriDetails.postcode + "<br/>" + eoriDetails.countryCode
                )
              )
            )
          )
        }

        "must create row for the applicant name" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.name.label")),
              Value(Text(contact.name))
            )
          )
        }

        "must create row for the applicant email" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.email.label")),
              Value(Text(contact.email))
            )
          )
        }

        "must create row for the applicant phone" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.phone.label")),
              Value(Text(contact.phone.get))
            )
          )
        }

        "must create row for the agent job title" in {
          result.eori.rows must contain(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.jobTitle.label")),
              Value(Text(contact.jobTitle.get))
            )
          )
        }

        "must create row for date submitted" in {
          result.eori.rows must contain(
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
            Value(Text(goodsDetails.goodsDescription))
          ),
          SummaryListRow(
            Key(Text("haveYouReceivedADecision.checkYourAnswersLabel")),
            Value(Text("site.yes"))
          ),
          SummaryListRow(
            Key(Text("tellUsAboutYourRuling.checkYourAnswersLabel")),
            Value(Text(goodsDetails.similarRulingMethodInfo.get))
          ),
          SummaryListRow(
            Key(Text("awareOfRuling.checkYourAnswersLabel")),
            Value(Text("site.yes"))
          ),
          SummaryListRow(
            Key(Text("aboutSimilarGoods.checkYourAnswersLabel")),
            Value(Text(goodsDetails.similarRulingGoodsInfo.get))
          ),
          SummaryListRow(
            Key(Text("hasCommodityCode.checkYourAnswersLabel")),
            Value(Text("site.yes"))
          ),
          SummaryListRow(
            Key(Text("commodityCode.checkYourAnswersLabel")),
            Value(Text(goodsDetails.envisagedCommodityCode.get))
          ),
          SummaryListRow(
            // LDS ignore
            Key(Text("haveTheGoodsBeenSubjectToLegalChallenges.checkYourAnswersLabel")),
            Value(Text("site.yes"))
          ),
          SummaryListRow(
            Key(Text("describeTheLegalChallenges.checkYourAnswersLabel")),
            Value(Text(goodsDetails.knownLegalProceedings.get))
          ),
          SummaryListRow(
            Key(Text("hasConfidentialInformation.checkYourAnswersLabel")),
            Value(Text("site.yes"))
          ),
          SummaryListRow(
            Key(Text("confidentialInformation.checkYourAnswersLabel")),
            Value(Text(goodsDetails.confidentialInformation.get))
          ),
          SummaryListRow(
            Key(Text("doYouWantToUploadDocuments.checkYourAnswersLabel")),
            Value(Text("site.no"))
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
        phoneNumber = None,
        isPrivate = None
      )
      val agentApplication =
        application.copy(agent = Some(agent), whatIsYourRoleResponse = Some(WhatIsYourRole.AgentOrg))
      val result           = ApplicationViewModel(agentApplication)

      "must create 10 rows" in {
        result.agent.get.rows.length must be(10)
      }

      "must create row for the applicant description of role" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswersForAgents.applicant.role.label")),
            Value(Text("whatIsYourRoleAsImporter.agentOnBehalfOfOrg"))
          )
        )
      }

      "must create row for the applicant name" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.applicant.name.label")),
            Value(Text(contact.name))
          )
        )
      }

      "must create row for the applicant email" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.applicant.email.label")),
            Value(Text(contact.email))
          )
        )
      }

      "must create row for the applicant phone" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.applicant.phone.label")),
            Value(Text(contact.phone.get))
          )
        )
      }

      "must create row for the applicant job title" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.applicant.jobTitle.label")),
            Value(Text(contact.jobTitle.get))
          )
        )
      }

      "must create row for the agent eori" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswersForAgents.agent.eori.number.label")),
            Value(Text(agent.eori))
          )
        )
      }

      "must create row for the agent business name" in {
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("checkYourAnswersForAgents.agent.name.label")),
            Value(Text(agent.businessName))
          )
        )
      }

      "must create row for the agent address" in {
        result.agent.get.rows must contain(
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
        result.agent.get.rows must contain(
          SummaryListRow(
            Key(Text("viewApplication.dateSubmitted")),
            Value(Text("22 August 2018"))
          )
        )
      }
    }

    "when given a valid application with agentTrader details" - {
      val publicTrader                                 = TraderDetail(
        eori = "agent eori",
        businessName = "agent business name",
        addressLine1 = "agent address line 1",
        addressLine2 = Some("AgentCity"),
        addressLine3 = None,
        postcode = "postcode",
        countryCode = "GB",
        phoneNumber = None,
        isPrivate = Some(false)
      )
      val privateTrader                                = publicTrader.copy(isPrivate = Some(true))
      val undefinedPrivacyTrader                       = publicTrader.copy(isPrivate = None)
      val agentTraderApplicationPublicTrader           = application.copy(
        agent = None,
        trader = publicTrader,
        whatIsYourRoleResponse = Some(WhatIsYourRole.AgentTrader)
      )
      val agentTraderApplicationPrivateTrader          =
        agentTraderApplicationPublicTrader.copy(trader = privateTrader)
      val agentTraderApplicationTraderPrivacyUndefined =
        agentTraderApplicationPublicTrader.copy(trader = undefinedPrivacyTrader)
      val resultPublic                                 = ApplicationViewModel(agentTraderApplicationPublicTrader)
      val resultPrivate                                = ApplicationViewModel(agentTraderApplicationPrivateTrader)
      val resultUndefinedPrivacy                       =
        ApplicationViewModel(agentTraderApplicationTraderPrivacyUndefined)
      "must contain trader business name for a public trader" in {
        resultPublic.eori.rows must contain(
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.trader.name.label")),
            Value(Text(publicTrader.businessName))
          )
        )
      }
      "must contain trader address for a public trader" in {
        resultPublic.eori.rows must contain(
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.trader.address.label")),
            Value(
              HtmlContent(
                publicTrader.addressLine1 + "<br/>" + publicTrader.addressLine2.get + "<br/>" + publicTrader.postcode + "<br/>" + publicTrader.countryCode
              )
            )
          )
        )
      }
      "must not contain trader business name for a private trader" in {
        resultPrivate.eori.rows must not contain SummaryListRow(
          Key(Text("agentForTraderCheckYourAnswers.trader.name.label")),
          Value(Text(privateTrader.businessName))
        )

      }
      "must not contain trader address for a private trader" in {
        resultPrivate.eori.rows must not contain
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.trader.address.label")),
            Value(
              HtmlContent(
                privateTrader.addressLine1 + "<br/>" + privateTrader.addressLine2.get + "<br/>" + privateTrader.postcode + "<br/>" + privateTrader.countryCode
              )
            )
          )
      }

      "must not contain trader business name for an undefined privacy trader" in {
        resultUndefinedPrivacy.trader.get.rows must not contain SummaryListRow(
          Key(Text("agentForTraderCheckYourAnswers.trader.name.label")),
          Value(Text(undefinedPrivacyTrader.businessName))
        )

      }
      "must not contain trader address for an undefined privacy trader" in {
        resultUndefinedPrivacy.trader.get.rows must not contain
          SummaryListRow(
            Key(Text("agentForTraderCheckYourAnswers.trader.address.label")),
            Value(
              HtmlContent(
                undefinedPrivacyTrader.addressLine1 + "<br/>" + undefinedPrivacyTrader.addressLine2.get + "<br/>" +
                  undefinedPrivacyTrader.postcode + "<br/>" + undefinedPrivacyTrader.countryCode
              )
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
    phoneNumber = None,
    isPrivate = Some(true)
  )

  val contact: ContactDetails = ContactDetails(
    name = "contact name",
    email = "email@example.com",
    phone = Some("phone"),
    companyName = Some("company name"),
    jobTitle = Some("job title")
  )

  val letterOfAuthorityRequest: Option[Attachment] = Some(
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

  val requestedMethod: MethodThree = MethodThree(
    whyNotOtherMethods = "method 3 why not",
    previousSimilarGoods = PreviousSimilarGoods("previous similar goods")
  )

  val goodsDetails: GoodsDetails = GoodsDetails(
    goodsDescription = "goods description",
    envisagedCommodityCode = Some("commodity code"),
    knownLegalProceedings = Some("legal"),
    confidentialInformation = Some("confidential"),
    similarRulingGoodsInfo = Some("goods info"),
    similarRulingMethodInfo = Some("methods info")
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
  val application: Application               =
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
      whatIsYourRoleResponse = Some(WhatIsYourRole.EmployeeOrg),
      letterOfAuthority = None
    )
}
