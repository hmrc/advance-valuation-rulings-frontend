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
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import connectors.BackendConnector
import models._
import models.AuthUserType._
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import models.requests.{ApplicationId, ApplicationSubmissionResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.mockito.internal.matchers.Any
import org.scalatest.{EitherValues, TryValues}
import pages._
import services.SubmissionService
import viewmodels.checkAnswers.summary.{ApplicationSummary, ApplicationSummaryService, DetailsSummary, IndividualApplicantSummary, IndividualEoriDetailsSummary, MethodSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersForAgentsView

class CheckYourAnswersForAgentsControllerSpec
    extends SpecBase
    with SummaryListFluency
    with EitherValues {

  "Check Your Answers for Agents Controller" - {

    "must return OK and the correct view for a GET as OrganisationAdmin" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        val appSummary = ApplicationSummary(
          IndividualEoriDetailsSummary(traderDetailsWithCountryCode, draftId)(stubMessages()),
          IndividualApplicantSummary(fullUserAnswers)(stubMessages()),
          DetailsSummary(fullUserAnswers)(stubMessages()),
          MethodSummary(fullUserAnswers)(stubMessages())
        )

        when(
          mockApplicationSummaryService.getApplicationSummary(
            any[UserAnswers],
            any[TraderDetailsWithCountryCode]
          )(any[Messages])
        ).thenReturn(appSummary)

        private val application = applicationBuilderAsOrg(userAnswers = Option(orgAdminUserAnswers))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[ApplicationSummaryService].toInstance(mockApplicationSummaryService)
          )
          .build()

        implicit val msgs: Messages = messages(application)

        running(application) {
          implicit val request: Request[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersForAgentsView]
          val list = mockApplicationSummaryService.getApplicationSummary(
            orgAdminUserAnswers,
            traderDetailsWithCountryCode
          )

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            list,
            EmployeeOfOrg,
            draftId
          ).toString
        }
      }

    "must return OK and the correct view for a GET as OrganisationAssistant claiming to be EmployeeOfOrg" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val userAnswers =
          orgAssistantUserAnswers.setFuture(WhatIsYourRoleAsImporterPage, EmployeeOfOrg).futureValue

        val appSummary = ApplicationSummary(
          IndividualEoriDetailsSummary(traderDetailsWithCountryCode, draftId)(stubMessages()),
          IndividualApplicantSummary(userAnswers)(stubMessages()),
          DetailsSummary(userAnswers)(stubMessages()),
          MethodSummary(userAnswers)(stubMessages())
        )

        when(
          mockApplicationSummaryService.getApplicationSummary(
            any[UserAnswers],
            any[TraderDetailsWithCountryCode]
          )(any[Messages])
        ).thenReturn(appSummary)

        private val application = applicationBuilderAsOrg(userAnswers = Option(userAnswers))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        implicit val msgs: Messages = messages(application)

        running(application) {
          implicit val request: Request[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result).contains("An employee of the organisation") mustEqual true;

        }
      }

    "must return OK and the correct view for a GET as OrganisationAssistant claiming to be AgentOnBehalfOfOrg" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val userAnswers = orgAssistantUserAnswers
          .setFuture(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfOrg)
          .futureValue

        val appSummary = ApplicationSummary(
          IndividualEoriDetailsSummary(traderDetailsWithCountryCode, draftId)(stubMessages()),
          IndividualApplicantSummary(userAnswers)(stubMessages()),
          DetailsSummary(userAnswers)(stubMessages()),
          MethodSummary(userAnswers)(stubMessages())
        )

        when(
          mockApplicationSummaryService.getApplicationSummary(
            any[UserAnswers],
            any[TraderDetailsWithCountryCode]
          )(any[Messages])
        ).thenReturn(appSummary)

        private val application = applicationBuilderAsOrg(userAnswers = Option(userAnswers))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        implicit val msgs: Messages = messages(application)

        running(application) {
          implicit val request: Request[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result).contains(
            "Agent acting on behalf of an organisation"
          ) mustEqual true;
        }
      }

    "must redirect to WhatIsYourRoleAsImporterPage for a GET as OrganisationAssistant if no importer role is found" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val application =
          applicationBuilderAsOrg(userAnswers = Option(orgAssistantUserAnswers))
            .overrides(
              bind[BackendConnector].toInstance(mockBackendConnector)
            )
            .build()

        private val redirectPage: String =
          routes.WhatIsYourRoleAsImporterController.onPageLoad(CheckMode, draftId).url

        runApplication(application, redirectPage)

      }

    "must redirect to WhatIsYourRoleAsImporterPage for a GET as Agent if no importer role is found" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val application = applicationBuilderAsAgent(userAnswers = Option(agentUserAnswers))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        private val redirectPage: String =
          routes.WhatIsYourRoleAsImporterController.onPageLoad(CheckMode, draftId).url

        runApplication(application, redirectPage)

      }

    "must redirect to UnauthorisedController for a GET as Trader" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val application = applicationBuilderAsAgent(userAnswers = Option(traderUserAnswers))
          .overrides(
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        private val redirectPage: String =
          routes.UnauthorisedController.onPageLoad.url

        runApplication(application, redirectPage)

      }

    "must redirect to Journey Recovery for a GET if no existing data is found for orgAssistant" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val application =
          applicationBuilderAsAgent(userAnswers = Some(orgAssistantUserAnswers)).build()

        runApplication(application, routes.JourneyRecoveryController.onPageLoad().url)

      }

    "must redirect to Journey Recovery for a GET if no existing data is found for agent" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val application =
          applicationBuilderAsAgent(userAnswers = Some(agentUserAnswers)).build()

        runApplication(application, routes.JourneyRecoveryController.onPageLoad().url)
      }

    "must redirect to Journey Recovery for a GET if no importer role is found" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val application = applicationBuilderAsOrg(userAnswers = None).build()

        runApplication(application, routes.JourneyRecoveryController.onPageLoad().url)
      }

    "must redirect to Application Complete when application submission succeeds" in
      new CheckYourAnswersForAgentsControllerSpecSetup {

        private val applicationId = ApplicationId(1)
        private val response      = ApplicationSubmissionResponse(applicationId)

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

        private val application = applicationBuilderAsOrg(Option(fullUserAnswers))
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[BackendConnector].toInstance(mockBackendConnector)
          )
          .build()

        private val redirectUrl =
          routes.ApplicationCompleteController.onPageLoad(applicationId.toString).url

        runApplication(application, redirectUrl, POST)
      }
  }

  "must redirect to JourneyRecoveryController when backend call fails" in
    new CheckYourAnswersForAgentsControllerSpecSetup {

      private val applicationId = ApplicationId(1)
      private val response      = ApplicationSubmissionResponse(applicationId)

      when(mockSubmissionService.submitApplication(any(), any())(any()))
        .thenReturn(Future.successful(response))
      when(
        mockBackendConnector.getTraderDetails(any(), any())(any(), any())
      ) thenReturn Future
        .successful(
          Left(BackendError(500, "Internal Server Error"))
        )

      private val application = applicationBuilderAsOrg(Option(fullUserAnswers))
        .overrides(
          bind[SubmissionService].toInstance(mockSubmissionService),
          bind[BackendConnector].toInstance(mockBackendConnector)
        )
        .build()

      private val redirectPage = routes.JourneyRecoveryController.onPageLoad().url
      runApplication(application, redirectPage)
    }

  private def runApplication(
    application: Application,
    redirectPage: String,
    httpRequest: String = GET
  ) =
    running(application) {
      val request =
        FakeRequest(httpRequest, routes.CheckYourAnswersForAgentsController.onPageLoad(draftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual redirectPage
    }

}

trait CheckYourAnswersForAgentsControllerSpecSetup extends MockitoSugar with TryValues {
  val userAnswersId: String = "id"
  val DraftIdSequence: Long = 123456789L
  val draftId: DraftId      = DraftId(DraftIdSequence)

  val orgAssistantUserAnswers: UserAnswers =
    UserAnswers(userAnswersId, draftId).set(AccountHomePage, OrganisationAssistant).success.get
  val agentUserAnswers: UserAnswers        =
    UserAnswers(userAnswersId, draftId).set(AccountHomePage, Agent).success.get
  val traderUserAnswers: UserAnswers       =
    UserAnswers(userAnswersId, draftId).set(AccountHomePage, IndividualTrader).success.get
  val orgAdminUserAnswers: UserAnswers     =
    UserAnswers(userAnswersId, draftId).set(AccountHomePage, OrganisationAdmin).success.get

  val mockSubmissionService: SubmissionService = mock[SubmissionService]
  val mockBackendConnector: BackendConnector   = mock[BackendConnector]

  val mockApplicationSummaryService = mock[ApplicationSummaryService]

  val contactInformation: ContactInformation = ContactInformation(
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

  val traderDetailsWithCountryCode: TraderDetailsWithCountryCode = TraderDetailsWithCountryCode(
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

  val fullUserAnswers: UserAnswers = (for {
    ua <- orgAssistantUserAnswers.set(DescriptionOfGoodsPage, "DescriptionOfGoodsPage")
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
              phone = "phone",
              None
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

  when(
    mockBackendConnector.getTraderDetails(any(), any())(any(), any())
  ).thenReturn(
    Future
      .successful(
        Right(
          traderDetailsWithCountryCode
        )
      )
  )

}
