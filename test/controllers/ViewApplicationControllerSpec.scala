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

import java.time.{Clock, Instant, ZoneOffset}

import scala.concurrent.Future

import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import connectors.BackendConnector
import generators.Generators
import models._
import models.requests._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import views.html.ViewApplicationView

class ViewApplicationControllerSpec extends SpecBase with MockitoSugar {
  import ViewApplicationControllerSpec._
  "ViewApplication Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockBackendConnector = mock[BackendConnector]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector)
        )
        .build()

      when(mockBackendConnector.getApplication(any())(any(), any()))
        .thenReturn(
          Future.successful(
            Right(ruling)
          )
        )

      implicit val msgs = messages(application)

      running(application) {
        val request = FakeRequest(
          GET,
          routes.ViewApplicationController.onPageLoad(ruling.id.toString).url
        )

        val applicationViewModel = ApplicationViewModel(applicationRequest)
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
  val randomString: String = stringsWithMaxLength(8).sample.get

  val eoriDetails = EORIDetails(
    eori = randomString,
    businessName = randomString,
    addressLine1 = randomString,
    addressLine2 = randomString,
    addressLine3 = "",
    postcode = randomString,
    country = randomString
  )

  val applicant = IndividualApplicant(
    contact = ContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString)
    )
  )

  val requestedMethod = MethodThree(
    whyNotOtherMethods = randomString,
    detailedDescription = PreviousSimilarGoods(randomString)
  )

  val goodsDetails = GoodsDetails(
    goodName = randomString,
    goodDescription = randomString,
    envisagedCommodityCode = Some(randomString),
    knownLegalProceedings = Some(randomString),
    confidentialInformation = Some(randomString)
  )

  val lastUpdated        = Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC))
  val lastUpdatedString  = "22/08/2018"
  val applicationRequest = ApplicationRequest(
    applicationNumber = ApplicationNumber("GBAVR", 1).render,
    eoriDetails = eoriDetails,
    applicant = applicant,
    requestedMethod = requestedMethod,
    goodsDetails = goodsDetails,
    attachments = Seq.empty
  )
  val applicationId      = ApplicationId(0L)
  val ruling             =
    Application(
      id = applicationId,
      request = applicationRequest,
      lastUpdated = lastUpdated,
      created = lastUpdated
    )

  val body =
    s"""{
    |"applicationNumber": "${applicationRequest.applicationNumber}",
    |"eoriDetails": {
    |  "eori": "$randomString",
    |  "businessName": "$randomString",
    |  "addressLine1": "$randomString",
    |  "addressLine2": "$randomString",
    |  "addressLine3": "",
    |  "postcode": "$randomString",
    |  "country": "$randomString"
    |},
    |"applicant": {
    |  "contact": {
    |    "name": "$randomString",
    |    "email": "$randomString",
    |    "phone": "$randomString"
    |  },
    |  "_type": "IndividualApplicant"
    |},
    |"requestedMethod" : {
    |  "whyNotOtherMethods" : "$randomString",
    |  "detailedDescription" : {
    |    "_value" : "$randomString",
    |    "_type" : "PreviousSimilarGoods"
    |  },
    |  "_type" : "MethodThree"
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
