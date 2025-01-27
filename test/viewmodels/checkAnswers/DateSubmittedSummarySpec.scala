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

package viewmodels.checkAnswers

import base.SpecBase
import models.requests._
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.{Clock, Instant, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.Locale

class DateSubmittedSummarySpec extends SpecBase {

  private val eoriDetails: TraderDetail = TraderDetail(
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

  private val contact: ContactDetails = ContactDetails(
    name = "contact name",
    email = "email@example.com",
    phone = Some("phone"),
    companyName = Some("company name"),
    jobTitle = Some("job title")
  )

  private val requestedMethod: MethodThree = MethodThree(
    whyNotOtherMethods = "method 3 why not",
    previousSimilarGoods = PreviousSimilarGoods("previous similar goods")
  )

  private val goodsDetails: GoodsDetails = GoodsDetails(
    goodsDescription = "goods description",
    envisagedCommodityCode = Some("commodity code"),
    knownLegalProceedings = Some("legal"),
    confidentialInformation = Some("confidential"),
    similarRulingGoodsInfo = Some("goods info"),
    similarRulingMethodInfo = Some("methods info")
  )

  private val lastUpdated: Instant = Instant.now(
    Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC)
  )

  private val applicationRequest: ApplicationRequest = ApplicationRequest(
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

  private val applicationId: ApplicationId = ApplicationId(0L)

  private val application: Application =
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

  ".row" - {

    Seq(
      ("en", Locale.forLanguageTag("en")),
      ("cy", Locale.getDefault)
    ).foreach { case (lang, locale) =>
      s"must create row for DateSubmittedSummary and format the date using the appropriate locale when the language is $lang" in {

        implicit val messages: Messages = stubMessagesApi().preferred(Seq(Lang(lang)))

        val expectedFormattedDate: String = DateTimeFormatter
          .ofPattern("dd MMMM yyyy")
          .withZone(ZoneId.systemDefault())
          .withLocale(locale)
          .format(application.created)

        DateSubmittedSummary.row(application) mustBe SummaryListRowViewModel(
          key = "viewApplication.dateSubmitted",
          value = ValueViewModel(expectedFormattedDate)
        )
      }
    }
  }
}
