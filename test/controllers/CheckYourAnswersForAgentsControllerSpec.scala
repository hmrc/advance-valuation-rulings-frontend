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

import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import connectors.BackendConnector
import models._
import models.AuthUserType.{OrganisationAdmin, OrganisationAssistant}
import models.requests.{ApplicationId, ApplicationSubmissionResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.EitherValues
import org.scalatest.TryValues
import pages._
import services.SubmissionService
import viewmodels.checkAnswers.summary.ApplicationSummary
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersForAgentsView

class CheckYourAnswersForAgentsControllerSpec
    extends SpecBase
    with SummaryListFluency
    with EitherValues {

  "Check Your Answers for Agents Controller" - {

    "must return OK and the correct view for a GET as OrganisationAdmin" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val ua: UserAnswers =
          emptyUserAnswers
            .set(AccountHomePage, OrganisationAdmin)
            .get

        val application = applicationBuilderAsOrg(userAnswers = Option(ua))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        implicit val msgs: Messages = messages(application)
        when(
          mockBackendConnector.getTraderDetails(any(), any())(any(), any())
        ) thenReturn Future
          .successful(
            Right(
              traderDetailsWithCountryCode
            )
          )

        running(application) {
          implicit val request =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersForAgentsView]
          val list = ApplicationSummary(ua, traderDetailsWithCountryCode)

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            list,
            authUserType = OrganisationAdmin,
            draftId
          ).toString
        }
      }

    "must return OK and the correct view for a GET as OrganisationAssistant" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val ua: UserAnswers =
          emptyUserAnswers
            .set(AccountHomePage, OrganisationAssistant)
            .get

        val application = applicationBuilderAsOrg(userAnswers = Option(ua))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        implicit val msgs: Messages = messages(application)
        when(
          mockBackendConnector.getTraderDetails(any(), any())(any(), any())
        ) thenReturn Future
          .successful(
            Right(
              traderDetailsWithCountryCode
            )
          )

        running(application) {
          implicit val request =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersForAgentsView]
          val list = ApplicationSummary(ua, traderDetailsWithCountryCode)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            list,
            authUserType = OrganisationAssistant,
            draftId
          ).toString
        }
      }

    "must redirect to Journey Recovery for a GET if no existing data is found" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val application = applicationBuilderAsOrg(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    "must redirect to Journey Recovery for a GET if no importer role is found" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val application = applicationBuilderAsOrg(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    "must redirect to Application Complete when application submission succeeds" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val applicationId = ApplicationId(1)
        val response      = ApplicationSubmissionResponse(applicationId)

        when(mockSubmissionService.submitApplication(any(), any())(any()))
          .thenReturn(Future.successful(response))
        when(
          mockBackendConnector.getTraderDetails(any(), any())(any(), any())
        ) thenReturn Future
          .successful(
            Right(
              traderDetailsWithCountryCode
            )
          )

        val application = applicationBuilderAsOrg(Option(fullUserAnswers))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.CheckYourAnswersForAgentsController.onSubmit(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ApplicationCompleteController
            .onPageLoad(applicationId.toString)
            .url
        }
      }

    "must redirect to JourneyRecoveryController when backend call fails" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val applicationId = ApplicationId(1)
        val response      = ApplicationSubmissionResponse(applicationId)

        when(mockSubmissionService.submitApplication(any(), any())(any()))
          .thenReturn(Future.successful(response))
        when(
          mockBackendConnector.getTraderDetails(any(), any())(any(), any())
        ) thenReturn Future
          .successful(
            Left(BackendError(500, "Internal Server Error"))
          )

        val application = applicationBuilderAsOrg(Option(fullUserAnswers))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.CheckYourAnswersForAgentsController.onSubmit(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController
            .onPageLoad()
            .url
        }
      }
  }
}

trait CheckYourAnswersForAgentsControllerSpecSetup extends MockitoSugar with TryValues {
  val userAnswersId: String         = "id"
  val DraftIdSequence               = 123456789L
  val draftId                       = DraftId(DraftIdSequence)
  val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, draftId)

  val mockSubmissionService = mock[SubmissionService]
  val mockBackendConnector  = mock[BackendConnector]

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

  val fullUserAnswers = (for {
    ua <- emptyUserAnswers.set(DescriptionOfGoodsPage, "DescriptionOfGoodsPage")
    ua <- ua.set(HasCommodityCodePage, false)
    ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
    ua <- ua.set(HasConfidentialInformationPage, false)
    ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)
    ua <- ua.set(
            CheckRegisteredDetailsPage,
            true
          )
    ua <- ua.set(
            BusinessContactDetailsPage,
            BusinessContactDetails(
              name = "name",
              email = "email",
              phone = "phone"
            )
          )
    ua <- ua.set(ValuationMethodPage, ValuationMethod.Method1)
    ua <- ua.set(IsThereASaleInvolvedPage, true)
    ua <- ua.set(IsSaleBetweenRelatedPartiesPage, true)
    ua <- ua.set(ExplainHowPartiesAreRelatedPage, "explainHowPartiesAreRelated")
    ua <- ua.set(AreThereRestrictionsOnTheGoodsPage, true)
    ua <- ua.set(DescribeTheRestrictionsPage, "describeTheRestrictions")
    ua <- ua.set(IsTheSaleSubjectToConditionsPage, false)
    ua <- ua.set(DoYouWantToUploadDocumentsPage, false)
  } yield ua).success.get
}
