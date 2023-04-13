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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, SummaryListRow}

import base.SpecBase
import com.softwaremill.quicklens._
import generators.Generators
import models._
import models.requests._
import org.scalacheck.Arbitrary

class ApplicationViewModelSpec extends SpecBase {
  import ApplicationViewModelSpec._

  "ApplicationViewModel should" - {

    implicit val m: Messages = play.api.test.Helpers.stubMessages()

    "when given a valid application" - {

      "when trader's consentToDisclosureOfPersonalData = true" - {
        val result = ApplicationViewModel(
          application.modify(_.trader.consentToDisclosureOfPersonalData).setTo(true)
        )

        "must create rows for the eori details" in {
          result.eori.rows must be(
            Seq(
              SummaryListRow(
                Key(Text("checkYourAnswers.eori.number.label")),
                Value(Text(randomString))
              ),
              SummaryListRow(
                Key(Text("checkYourAnswers.eori.name.label")),
                Value(Text(randomString))
              ),
              SummaryListRow(
                Key(Text("checkYourAnswers.eori.address.label")),
                Value(
                  HtmlContent(
                    s"${randomString}<br>${randomString}<br>${randomString}<br>${randomString}"
                  )
                )
              )
            )
          )
        }
      }

      "when trader's consentToDisclosureOfPersonalData = false" - {
        val result = ApplicationViewModel(
          application.modify(_.trader.consentToDisclosureOfPersonalData).setTo(false)
        )

        "must create rows for the eori details" in {
          result.eori.rows must be(
            Seq(
              SummaryListRow(
                Key(Text("checkYourAnswers.eori.number.label")),
                Value(Text(randomString))
              )
            )
          )
        }
      }

      "must create row for the applicant" in {
        val result = ApplicationViewModel(application)

        result.applicant.rows must be(
          Seq(
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.name.label")),
              Value(Text(randomString))
            ),
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.email.label")),
              Value(Text(randomString))
            ),
            SummaryListRow(
              Key(Text("checkYourAnswers.applicant.phone.label")),
              Value(Text(randomString))
            ),
            SummaryListRow(
              Key(Text("viewApplication.dateSubmitted")),
              Value(Text("22 August 2018"))
            )
          )
        )
      }
    }
  }
}

object ApplicationViewModelSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val randomBoolean: Boolean = Arbitrary.arbitrary[Boolean].sample.getOrElse(true)

  val eoriDetails = TraderDetail(
    eori = randomString,
    consentToDisclosureOfPersonalData = randomBoolean,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = randomString,
    countryCode = randomString,
    phoneNumber = None
  )

  val contact = ContactDetails(
    name = randomString,
    email = randomString,
    phone = Some(randomString)
  )

  val requestedMethod = MethodThree(
    whyNotOtherMethods = randomString,
    previousSimilarGoods = PreviousSimilarGoods(randomString)
  )

  val goodsDetails = GoodsDetails(
    goodsName = randomString,
    goodsDescription = randomString,
    envisagedCommodityCode = Some(randomString),
    knownLegalProceedings = Some(randomString),
    confidentialInformation = Some(randomString)
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
    attachments = Seq.empty
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
      attachments = applicationRequest.attachments
    )
}
