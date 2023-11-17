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

package controllers

import base.SpecBase
import connectors.BackendConnector
import generators.Generators
import models._
import models.requests._
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.ApplicationViewModel
import views.html.ViewApplicationView

import java.time.{Clock, Instant, ZoneOffset}
import scala.collection.immutable.List.from
import scala.concurrent.Future

class ViewApplicationControllerSpec extends SpecBase {
  import ViewApplicationControllerSpec._
  "ViewApplication Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockBackendConnector = mock[BackendConnector]

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector)
        )
        .build()

      when(mockBackendConnector.getApplication(any())(any()))
        .thenReturn(Future.successful(ruling))

      implicit val msgs: Messages = messages(application)

      running(application) {
        val request = FakeRequest(
          GET,
          routes.ViewApplicationController.onPageLoad(ruling.id.toString).url
        )

        val applicationViewModel = ApplicationViewModel(ruling)
        val result               = route(application, request).value
        val view                 = application.injector.instanceOf[ViewApplicationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          applicationViewModel,
          ruling.id.toString,
          lastUpdatedString
        )(
          request,
          messages(application)
        ).toString
      }
    }
  }
}

object ViewApplicationControllerSpec extends Generators {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val randomString: String = stringsWithMaxLength(8).sample.get

  val eoriDetails: TraderDetail = TraderDetail(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = Some(randomString),
    addressLine3 = None,
    postcode = randomString,
    countryCode = randomString,
    phoneNumber = None,
    isPrivate = Some(false)
  )

  val contact: ContactDetails = ContactDetails(
    name = randomString,
    email = randomString,
    phone = Some(randomString),
    companyName = Some(randomString),
    jobTitle = Some(randomString)
  )

  val requestedMethod: MethodThree = MethodThree(
    whyNotOtherMethods = randomString,
    previousSimilarGoods = PreviousSimilarGoods(randomString)
  )

  val goodsDetails: GoodsDetails = GoodsDetails(
    goodsName = randomString,
    goodsDescription = randomString,
    envisagedCommodityCode = Some(randomString),
    knownLegalProceedings = Some(randomString),
    confidentialInformation = Some(randomString),
    similarRulingGoodsInfo = Some(randomString),
    similarRulingMethodInfo = Some(randomString)
  )

  val lastUpdated: Instant                   =
    Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC))
  val lastUpdatedString: String              = "22/08/2018"
  val draftId: DraftId                       = DraftId(0L)
  val applicationRequest: ApplicationRequest = ApplicationRequest(
    draftId = draftId,
    trader = eoriDetails,
    agent = None,
    contact = contact,
    requestedMethod = requestedMethod,
    goodsDetails = goodsDetails,
    attachments = Nil,
    whatIsYourRole = WhatIsYourRole.AgentTrader,
    letterOfAuthority = Some(AttachmentRequest("bob", None, "url", Privacy.Public, "jpg", 12L))
  )
  val applicationId: ApplicationId           = ApplicationId(0L)
  val ruling: Application                    =
    Application(
      id = applicationId,
      lastUpdated = lastUpdated,
      created = lastUpdated,
      trader = applicationRequest.trader,
      agent = applicationRequest.agent,
      contact = applicationRequest.contact,
      requestedMethod = applicationRequest.requestedMethod,
      goodsDetails = applicationRequest.goodsDetails,
      attachments = from(Nil),
      whatIsYourRoleResponse = Some(WhatIsYourRole.AgentTrader),
      letterOfAuthority = Some(
        Attachment(0x4L, "bob", None, "the location", Privacy.Public, "application/jpg", 4532L)
      )
    )

  val body: String =
    s"""{
    |"draftId": "$draftId",
    |"eoriDetails": {
    |  "eori": "$randomString",
    |  "businessName": "$randomString",
    |  "addressLine1": "$randomString",
    |  "addressLine2": "$randomString",
    |  "addressLine3": "",
    |  "postcode": "$randomString",
    |  "country": "$randomString"
    |},
    |"contact": {
    |  "name": "$randomString",
    |  "email": "$randomString",
    |  "phone": "$randomString"
    |},
    |"requestedMethod" : {
    |  "whyNotOtherMethods" : "$randomString",
    |  "detailedDescription" : {
    |    "_value" : "$randomString",
    |    "type" : "PreviousSimilarGoods"
    |  },
    |  "type" : "MethodThree"
    |},
    |"goodsDetails": {
    |  "goodDescription": "$randomString",
    |  "envisagedCommodityCode": "$randomString",
    |  "knownLegalProceedings": "$randomString",
    |  "confidentialInformation": "$randomString"
    |},
    |"attachments": []
    }""".stripMargin
}
