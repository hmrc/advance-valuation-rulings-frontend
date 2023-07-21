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

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.Future

import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier

import akka.util.Timeout
import base.SpecBase
import com.typesafe.play.cachecontrol.Seconds.ZERO.seconds
import config.FrontendAppConfig
import connectors.BackendConnector
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import forms.CheckRegisteredDetailsFormProvider
import models._
import models.requests.DataRequest
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq, same}
import org.mockito.Mockito.when
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages.{AgentForTraderCheckRegisteredDetailsPage, CheckRegisteredDetailsPage}
import services.UserAnswersService
import userrole.{UserRole, UserRoleProvider}

class CheckRegisteredDetailsControllerSpec
    extends SpecBase
    with MockitoSugar
    with TableDrivenPropertyChecks {

  lazy val checkRegisteredDetailsRoute =
    routes.CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId).url

  val formProvider = new CheckRegisteredDetailsFormProvider()
  val form         = formProvider()

  val contactInformation = ContactInformation(
    personOfContact = Some("Test Person"),
    sepCorrAddrIndicator = Some(false),
    streetAndNumber = Some("Test Street 1"),
    city = Some("Test City"),
    postalCode = Some("Test Postal Code"),
    countryCode = Some("GB"),
    telephoneNumber = Some("Test Telephone Number"),
    faxNumber = Some("Test Fax Number"),
    emailAddress = Some("Test Email Address"),
    emailVerificationTimestamp = Some("2000-01-31T23:59:59Z")
  )

  val traderDetailsWithCountryCode = TraderDetailsWithCountryCode(
    EORINo = "GB123456789012345",
    consentToDisclosureOfPersonalData = true,
    CDSFullName = "Test Name",
    CDSEstablishmentAddress = CDSEstablishmentAddress(
      streetAndNumber = "Test Street 1",
      city = "Test City",
      countryCode = "GB",
      postalCode = Some("Test Postal Code")
    ),
    contactInformation = Some(contactInformation)
  )

  val userAnswers = userAnswersAsIndividualTrader
    .set(CheckRegisteredDetailsPage, true)
    .success
    .value

  val consentToDisclosureOfPersonalDataScenarios =
    Table("consentToDisclosureOfPersonalData", true, false)

  val mockBackendConnector   = mock[BackendConnector]
  val mockUserAnswersService = mock[UserAnswersService]
  val mockUserRoleProvider   = mock[UserRoleProvider]
  val mockUserRole           = mock[UserRole]
  val mockAppConfig          = mock[FrontendAppConfig]

  when(mockAppConfig.agentOnBehalfOfTrader).thenReturn(true)

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

    forAll(consentToDisclosureOfPersonalDataScenarios) {
      consentValue =>
        s"must return OK and the correct view for a GET when consentToDisclosureOfPersonalData is $consentValue" in {

          val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
            .overrides(
              bind[BackendConnector].toInstance(mockBackendConnector),
              bind[UserAnswersService].toInstance(mockUserAnswersService),
              bind[UserRoleProvider].toInstance(mockUserRoleProvider),
              bind[FrontendAppConfig].toInstance(mockAppConfig)
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
                bind[UserRoleProvider].toInstance(mockUserRoleProvider),
                bind[FrontendAppConfig].toInstance(mockAppConfig)
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
            bind[UserRoleProvider].toInstance(mockUserRoleProvider),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
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
            bind[UserRoleProvider].toInstance(mockUserRoleProvider),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.UploadLetterController
          .onPageLoad(draftId)
          .url
      }
    }

    "must redirect to the kickout page when data is submitted with No radio button" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
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
            bind[UserRoleProvider].toInstance(mockUserRoleProvider),
            bind[FrontendAppConfig].toInstance(mockAppConfig)
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
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

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

      val traderDetails =
        traderDetailsWithCountryCode.copy(consentToDisclosureOfPersonalData = true)

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector),
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind[UserRoleProvider].toInstance(mockUserRoleProvider),
          bind[FrontendAppConfig].toInstance(mockAppConfig)
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

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector),
          bind[FrontendAppConfig].toInstance(mockAppConfig)
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
