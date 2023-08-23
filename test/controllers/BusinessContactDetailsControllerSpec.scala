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

import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.BusinessContactDetailsFormProvider
import models.{BusinessContactDetails, Done, NormalMode, WhatIsYourRoleAsImporter}
import models.AuthUserType.IndividualTrader
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccountHomePage, BusinessContactDetailsPage}
import services.UserAnswersService
import views.html.BusinessContactDetailsView

class BusinessContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new BusinessContactDetailsFormProvider()
  val defaultForm  = formProvider(false)
  val longForm     = formProvider(true)

  lazy val businessContactDetailsRoute =
    routes.BusinessContactDetailsController.onPageLoad(NormalMode, draftId).url
  lazy val saveDraftRoute: String      =
    routes.BusinessContactDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = true)
      .url

  lazy val continueRoute: String =
    routes.BusinessContactDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = false)
      .url

  val userAnswersForTest = userAnswersForRole(WhatIsYourRoleAsImporter.EmployeeOfOrg)
    .setFuture(AccountHomePage, IndividualTrader)
    .futureValue

  "BusinessContactDetails Controller" - {

    "Redirects to Draft saved page when save-draft is selected" in {

      val application: Application =
        setupTestBuild(userAnswersForTest)

      running(application) {
        val request =
          FakeRequest(POST, saveDraftRoute)
            .withFormUrlEncodedBody(
              ("name", "my name"),
              ("email", "email@example.co.uk"),
              ("phone", "07123456789")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Call(
          "POST",
          s"/advance-valuation-ruling/$draftId/save-as-draft"
        ).url
      }
    }
    "must return OK and the correct view for a GET as a standard user" in {

      val application =
        applicationBuilderAsAgent(userAnswers = Some(userAnswersForTest)).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(defaultForm, NormalMode, draftId, false)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered (default)" in {

      val businessContactDetails = BusinessContactDetails(
        name = "name",
        email = "abc@email.com",
        phone = "0123456789",
        companyName = None
      )

      val userAnswers =
        userAnswersForTest
          .set(BusinessContactDetailsPage, businessContactDetails)
          .success
          .value

      val application = applicationBuilderAsAgent(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          longForm.fill(businessContactDetails),
          NormalMode,
          draftId,
          false
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered (expanded)" in {

      val businessContactDetails = BusinessContactDetails(
        name = "name",
        email = "abc@email.com",
        phone = "0123456789",
        companyName = Some("test company")
      )

      val userAnswers =
        userAnswersForRole(WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader)
          .set(AccountHomePage, IndividualTrader)
          .get
          .set(BusinessContactDetailsPage, businessContactDetails)
          .get

      val application = applicationBuilderAsAgent(userAnswers = Some(userAnswers))
        .configure("features.agent-on-behalf-of-trader" -> true)
        .build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          longForm.fill(businessContactDetails),
          NormalMode,
          draftId,
          true
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must return OK and the correct view for a GET as an agent for traderuser" in {

      val userAnswers =
        userAnswersForRole(WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader)
          .set(AccountHomePage, IndividualTrader)
          .get

      val application =
        applicationBuilderAsAgent(userAnswers = Some(userAnswers))
          .configure("features.agent-on-behalf-of-trader" -> true)
          .build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(longForm, NormalMode, draftId, true)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilderAsAgent(userAnswers = Some(userAnswersForTest))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(
              ("name", "my name"),
              ("email", "email@example.co.uk"),
              ("phone", "07123456789")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilderAsAgent(userAnswers = Some(userAnswersForTest)).build()

      running(application) {
        val request =
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = defaultForm.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, draftId, false)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilderAsAgent(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilderAsAgent(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, continueRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
