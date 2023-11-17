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
import forms.EmployeeCheckRegisteredDetailsFormProvider
import models._
import models.requests.DataRequest
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, same}
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.prop.TableDrivenPropertyChecks
import pages.{AgentForTraderCheckRegisteredDetailsPage, CheckRegisteredDetailsPage, WhatIsYourRoleAsImporterPage}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.UserAnswersService
import userrole.{UserRole, UserRoleProvider}

import scala.concurrent.Future

class CheckRegisteredDetailsControllerSpec extends SpecBase with TableDrivenPropertyChecks {

  private lazy val checkRegisteredDetailsRoute =
    routes.CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId).url

  private val userAnswers = userAnswersAsIndividualTrader
    .set(CheckRegisteredDetailsPage, true)
    .success
    .value
    .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)
    .get

  private val consentToDisclosureOfPersonalDataScenarios =
    Table("consentToDisclosureOfPersonalData", true, false)

  private val mockBackendConnector   = mock[BackendConnector]
  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockUserRoleProvider   = mock[UserRoleProvider]
  private val mockUserRole           = mock[UserRole]

  private def setUpBackendConnectorMock(isInternalServerError: Boolean = false) =
    if (isInternalServerError) {
      when(
        mockBackendConnector.getTraderDetails(any(), any())(any(), any())
      ) thenReturn Future.successful(
        Left(BackendError(code = 500, message = "some backed error"))
      )
    } else {
      when(
        mockBackendConnector.getTraderDetails(any(), any())(any(), any())
      ) thenReturn Future
        .successful(
          Right(
            traderDetailsWithCountryCode
          )
        )
    }

  private def setUpUserAnswersServiceMock(answers: UserAnswers) = {
    when(mockUserAnswersService.get(any())(any()))
      .thenReturn(Future.successful(Some(answers)))
    when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))
  }

  private def setUpUserRoleProviderMock() = {
    when(mockUserRoleProvider.getUserRole(any())).thenReturn(mockUserRole)
    when(mockUserRole.selectGetRegisteredDetailsPage())
      .thenReturn(AgentForTraderCheckRegisteredDetailsPage)
    when(mockUserRole.getFormForCheckRegisteredDetails)
      .thenReturn(new EmployeeCheckRegisteredDetailsFormProvider()())
  }

  private def setUpViewMockForUserRole(expectedViewBody: String = "") = {
    val expectedView = HtmlFormat.raw(expectedViewBody)
    when(
      mockUserRole.selectViewForCheckRegisteredDetails(
        ArgumentMatchers.any[Form[Boolean]],
        same(traderDetailsWithCountryCode),
        same(NormalMode),
        ArgumentMatchers.eq(this.draftId)
      )(any[DataRequest[AnyContent]], any[Messages])
    ).thenReturn(expectedView)
  }

  "CheckRegisteredDetails Controller" - {

    setUpUserAnswersServiceMock(userAnswers)
    setUpBackendConnectorMock()
    setUpUserRoleProviderMock()
    setUpViewMockForUserRole()

    forAll(consentToDisclosureOfPersonalDataScenarios) { consentValue =>
      s"must return OK and the correct view for a GET when consentToDisclosureOfPersonalData is $consentValue" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, checkRegisteredDetailsRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }

      s"must return correct view for a GET when question has been answered previously and consentToDisclosureOfPersonalData is $consentValue" in {

        val previousUserAnswers = userAnswersAsIndividualTrader
          .set(
            CheckRegisteredDetailsPage,
            consentValue
          )
          .success
          .value

        setUpUserAnswersServiceMock(previousUserAnswers)

        val application =
          applicationBuilder(userAnswers = Some(previousUserAnswers))
            .overrides(
              bind[UserAnswersService].toInstance(mockUserAnswersService),
              bind[BackendConnector].toInstance(mockBackendConnector),
              bind[UserRoleProvider].toInstance(mockUserRoleProvider)
            )
            .build()

        running(application) {
          val request = FakeRequest(GET, checkRegisteredDetailsRoute)

          val result = route(application, request).value

          status(result) mustEqual OK
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when data is submitted with Yes radio button" in {
      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UploadLetterOfAuthorityController
          .onPageLoad(NormalMode, draftId, None, None, redirectedFromChangeButton = false)
          .url
      }
    }

    "must redirect to the kickout page when data is submitted with No radio button" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.EORIBeUpToDateController
          .onPageLoad(draftId)
          .url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setUpViewMockForUserRole()

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to Journey Recovery on submit when user has no answers" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkRegisteredDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "show the view given by the UserRole" in {

      val expectedViewBody = "hello"
      setUpViewMockForUserRole(expectedViewBody)

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector),
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind[UserRoleProvider].toInstance(mockUserRoleProvider)
        )
        .build()

      running(application) {
        val request        = FakeRequest(GET, checkRegisteredDetailsRoute)
        val result         = route(application, request).value
        val actualViewBody = contentAsString(result)

        actualViewBody mustBe expectedViewBody
      }

    }

    "redirect to Journey Recovery if the registered details retrieval from backend fails" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector)
        )
        .build()

      setUpBackendConnectorMock(isInternalServerError = true)

      running(application) {
        val request = FakeRequest(GET, checkRegisteredDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

  }
}
