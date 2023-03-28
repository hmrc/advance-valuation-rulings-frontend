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
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

import base.SpecBase
import generators.Generators
import models.UserAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ApplicationContactDetailsPage, BusinessContactDetailsPage}
import viewmodels.checkAnswers.summary.ApplicationSummary
import views.html.ApplicationCompleteView

class ApplicationCompleteControllerSpec extends SpecBase with Generators {

  "ApplicationComplete Controller" - {

    "must return OK and the correct view for a GET for Individual" in {
      ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
        ua =>
          val answers =
            addEmailToUserAnswers(ApplicationContactDetailsPage.toString, ua, ContactEmail)

          val application = applicationBuilder(Option(answers)).build()

          running(application) {
            implicit val msgs = messages(application)

            val url     = routes.ApplicationCompleteController.onPageLoad(ua.applicationNumber).url
            val request = FakeRequest(GET, url)
            val view    = application.injector.instanceOf[ApplicationCompleteView]
            val summary = ApplicationSummary(answers, Individual).removeActions()

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(ua.applicationNumber, ContactEmail, summary)(
              request,
              messages(application)
            ).toString
          }
      }
    }

    "must return OK and the correct view for a GET for Agent" in {
      ScalaCheckPropertyChecks.forAll(arbitraryUserData.arbitrary) {
        ua =>
          val answers = addEmailToUserAnswers(BusinessContactDetailsPage.toString, ua, ContactEmail)

          val application = applicationBuilderAsAgent(Option(answers)).build()

          running(application) {
            implicit val msgs = messages(application)

            val url     = routes.ApplicationCompleteController.onPageLoad(ua.applicationNumber).url
            val request = FakeRequest(GET, url)
            val view    = application.injector.instanceOf[ApplicationCompleteView]
            val summary = ApplicationSummary(answers, Organisation).removeActions()

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(ua.applicationNumber, ContactEmail, summary)(
              request,
              messages(application)
            ).toString
          }
      }
    }
  }

  private def addEmailToUserAnswers(
    contactDetailsFieldName: String,
    ua: UserAnswers,
    Email: String
  ) = {
    val emailUpdate   = (__ \ contactDetailsFieldName \ "email").json.put(JsString(Email))
    val dataWithEmail = ua.data.transform(__.json.update(emailUpdate)).get
    ua.copy(data = dataWithEmail)
  }
}
