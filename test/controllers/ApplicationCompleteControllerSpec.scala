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

import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import generators.Generators
import models.ValuationMethod
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ValuationMethodPage
import viewmodels.checkAnswers.summary.ApplicationSummary
import views.html.ApplicationCompleteView

class ApplicationCompleteControllerSpec extends SpecBase with Generators {

  "ApplicationComplete Controller" - {

    "must return OK and the correct view for a GET" in {
      ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
        ua =>
          val userAnswers       = ua.set(ValuationMethodPage, ValuationMethod.Method2).success.value
          val Email             = "testEmail@mail.com"
          val applicationNumber = userAnswers.applicationNumber

          val emailUpdate   = (__ \ "applicationContactDetails" \ "email").json.put(JsString(Email))
          val dataWithEmail = userAnswers.data.transform(__.json.update(emailUpdate)).get
          val answers       = userAnswers.copy(data = dataWithEmail)
          val application   = applicationBuilder(userAnswers = Option(answers)).build()

          running(application) {
            val request       =
              FakeRequest(
                GET,
                routes.ApplicationCompleteController.onPageLoad(applicationNumber).url
              )
            implicit val msgs = messages(application)
            val result        = route(application, request).value

            val view    = application.injector.instanceOf[ApplicationCompleteView]
            val summary = ApplicationSummary(userAnswers, AffinityGroup.Individual).removeActions
            status(result) mustEqual OK
            contentAsString(result) mustEqual view(applicationNumber, Email, summary)(
              request,
              messages(application)
            ).toString
          }
      }
    }
  }
}
