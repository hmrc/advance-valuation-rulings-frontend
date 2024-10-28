/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.BusinessContactDetailsFormProvider
import models.AuthUserType.IndividualTrader
import models.{BusinessContactDetails, Done, NormalMode, WhatIsYourRoleAsImporter}
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import pages.{AccountHomePage, BusinessContactDetailsPage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.BusinessContactDetailsView

import scala.concurrent.Future

class BusinessContactDetailsControllerSpec extends SpecBase {

  private val formProvider = new BusinessContactDetailsFormProvider()
  private val defaultForm  = formProvider(false)
  private val longForm     = formProvider(true)

  private lazy val businessContactDetailsRoute =
    routes.BusinessContactDetailsController.onPageLoad(NormalMode, draftId).url
  private lazy val saveDraftRoute: String      =
    routes.BusinessContactDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = true)
      .url

  private lazy val continueRoute: String =
    routes.BusinessContactDetailsController
      .onSubmit(NormalMode, draftId, saveDraft = false)
      .url

  private val userAnswersForTest = userAnswersForRole(WhatIsYourRoleAsImporter.EmployeeOfOrg)
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
              ("phone", "07123456789"),
              ("jobTitle", "CEO")
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
        contentAsString(result) mustEqual view(
          defaultForm,
          NormalMode,
          draftId,
          includeCompanyName = false
        )(
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
        companyName = None,
        jobTitle = "CEO"
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
          includeCompanyName = false
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
        companyName = Some("test company"),
        jobTitle = "CEO"
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
          includeCompanyName = true
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
        contentAsString(result) mustEqual view(
          longForm,
          NormalMode,
          draftId,
          includeCompanyName = true
        )(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersService = mock(classOf[UserAnswersService])

      when(mockUserAnswersService.set(any())(any())) `thenReturn` Future.successful(Done)

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
              ("phone", "07123456789"),
              ("jobTitle", "CEO")
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
        contentAsString(result) mustEqual view(
          boundForm,
          NormalMode,
          draftId,
          includeCompanyName = false
        )(
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
