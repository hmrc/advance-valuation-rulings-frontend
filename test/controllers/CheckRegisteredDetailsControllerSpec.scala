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
import uk.gov.hmrc.auth.core.AffinityGroup

import base.SpecBase
import connectors.BackendConnector
import forms.CheckRegisteredDetailsFormProvider
import models._
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages.CheckRegisteredDetailsPage
import services.UserAnswersService

class CheckRegisteredDetailsControllerSpec
    extends SpecBase
    with MockitoSugar
    with TableDrivenPropertyChecks {

  def onwardRoute = Call("GET", "/foo")

  lazy val checkRegisteredDetailsRoute =
    routes.CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId).url

  val formProvider = new CheckRegisteredDetailsFormProvider()
  val form         = formProvider(AffinityGroup.Individual, true)

  "CheckRegisteredDetails Controller" - {

    val registeredDetails: CheckRegisteredDetails = CheckRegisteredDetails(
      value = false,
      eori = "GB123456789012345",
      consentToDisclosureOfPersonalData = true,
      name = "Test Name",
      streetAndNumber = "Test Street 1",
      city = "Test City",
      country = "Test Country",
      postalCode = Some("Test Postal Code"),
      phoneNumber = Some("Test Telephone Number")
    )

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
      EORINo = registeredDetails.eori,
      consentToDisclosureOfPersonalData = true,
      CDSFullName = registeredDetails.name,
      CDSEstablishmentAddress = CDSEstablishmentAddress(
        streetAndNumber = registeredDetails.streetAndNumber,
        city = registeredDetails.city,
        countryCode = "GB",
        postalCode = registeredDetails.postalCode
      ),
      contactInformation = Some(contactInformation)
    )

    val userAnswers = emptyUserAnswers
      .set(CheckRegisteredDetailsPage, registeredDetails)
      .success
      .value

    val consentToDisclosureOfPersonalDataScenarios =
      Table("consentToDisclosureOfPersonalData", true, false)

    forAll(consentToDisclosureOfPersonalDataScenarios) {
      consentValue =>
        s"must return OK and the correct view for a GET when consentToDisclosureOfPersonalData is $consentValue" in {

          val mockBackendConnector   = mock[BackendConnector]
          val mockUserAnswersService = mock[UserAnswersService]

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[BackendConnector].toInstance(mockBackendConnector),
              bind[UserAnswersService].toInstance(mockUserAnswersService)
            )
            .build()

          when(
            mockBackendConnector.getTraderDetails(any(), any())(any(), any())
          ) thenReturn Future
            .successful(
              Right(
                traderDetailsWithCountryCode.copy(consentToDisclosureOfPersonalData = consentValue)
              )
            )

          when(mockUserAnswersService.get(any())(any()))
            .thenReturn(Future.successful(Some(userAnswers)))
          when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

          running(application) {
            val request = FakeRequest(GET, checkRegisteredDetailsRoute)

            val result = route(application, request).value

            status(result) mustEqual OK
          }
        }

        s"must return correct view for a GET when question has been answered previously and consentToDisclosureOfPersonalData is $consentValue" in {

          val mockUserAnswersService = mock[UserAnswersService]
          val previousUserAnswers    = emptyUserAnswers
            .set(
              CheckRegisteredDetailsPage,
              registeredDetails.copy(consentToDisclosureOfPersonalData = consentValue)
            )
            .success
            .value

          when(mockUserAnswersService.get(any())(any()))
            .thenReturn(Future.successful(Some(previousUserAnswers)))

          val application =
            applicationBuilder(userAnswers = Some(previousUserAnswers))
              .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
              .build()

          running(application) {
            val request = FakeRequest(GET, checkRegisteredDetailsRoute)

            val result = route(application, request).value

            status(result) mustEqual OK
          }
        }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
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

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]

      val userAnswers = emptyUserAnswers
        .set(CheckRegisteredDetailsPage, registeredDetails)
        .success
        .value

      when(mockUserAnswersService.get(any())(any()))
        .thenReturn(Future.successful(Some(userAnswers)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

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

    "redirect to Journey Recovery if the registered details retrieval from backend fails" in {

      val mockBackendConnector = mock[BackendConnector]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector)
        )
        .build()

      when(
        mockBackendConnector.getTraderDetails(any(), any())(any(), any())
      ) thenReturn Future.successful(
        Left(BackendError(code = 500, message = "some backed error"))
      )

      running(application) {
        val request = FakeRequest(GET, checkRegisteredDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
