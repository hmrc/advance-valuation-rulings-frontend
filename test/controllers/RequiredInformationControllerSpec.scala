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

import scala.concurrent.Future

import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import models.{Done, RequiredInformation}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.UserAnswersService
import views.html.{RequiredInformationView, TraderAgentRequiredInformationView}

class RequiredInformationControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val requiredInformationRoute =
    routes.RequiredInformationController.onPageLoad(draftId).url

  "RequiredInformation Controller" - {

    "must return OK and the correct view for individual user type" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, requiredInformationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RequiredInformationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(draftId)(
          request,
          messages(application)
        ).toString
      }
    }
    "must return OK and the correct view for non individual user type" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsOrgAdmin)).build()

      running(application) {
        val request = FakeRequest(GET, requiredInformationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TraderAgentRequiredInformationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(draftId)(
          request,
          messages(application)
        ).toString
      }
    }

  }
}
