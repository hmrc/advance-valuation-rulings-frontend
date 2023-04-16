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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
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

    "when given a valid application" - {

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

      "must create rows for the applicant" in {
        result.applicant.rows must be(
          Seq(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.name.label")),
              Value(Text(contact.name))
            ),
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.email.label")),
              Value(Text(contact.email))
            ),
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.phone.label")),
              Value(Text(contact.phone.get))
            ),
            SummaryListRow(
              Key(Text("viewApplication.dateSubmitted")),
              Value(Text("22 August 2018"))
            )
          )
        )
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
  }
}

object ApplicationViewModelSpec extends Generators {

  val eoriDetails = TraderDetail(
    eori = "eori",
    businessName = "business name",
    addressLine1 = "address line 1",
    addressLine2 = Some("address line 2"),
    addressLine3 = None,
    postcode = "postcode",
    countryCode = "country code",
    phoneNumber = None
  )

  val contact = ContactDetails(
    name = "contact name",
    email = "email@example.com",
    phone = Some("phone")
  )

  val requestedMethod = MethodThree(
    whyNotOtherMethods = "method 3 why not",
    previousSimilarGoods = PreviousSimilarGoods("previous similar goods")
  )

  val goodsDetails = GoodsDetails(
    goodsName = "goods name",
    goodsDescription = "goods description",
    envisagedCommodityCode = Some("commodity code"),
    knownLegalProceedings = Some("legal"),
    confidentialInformation = Some("confidential")
  )

  val lastUpdated        = Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC))
  val lastUpdatedString  = "22/08/2018"
  val draftId            = DraftId(0)
  val applicationRequest = ApplicationRequest(
    draftId = draftId,
    trader = eoriDetails,
    agent = None,
    contact = contact,
    requestedMethod = requestedMethod,
    goodsDetails = goodsDetails,
    attachments = Nil
  )
  val applicationId      = ApplicationId(0L)
  val application        =
    Application(
      id = applicationId,
      lastUpdated = lastUpdated,
      created = lastUpdated,
      trader = applicationRequest.trader,
      agent = applicationRequest.agent,
      contact = applicationRequest.contact,
      requestedMethod = applicationRequest.requestedMethod,
      goodsDetails = applicationRequest.goodsDetails,
      attachments = Nil
    )
}
