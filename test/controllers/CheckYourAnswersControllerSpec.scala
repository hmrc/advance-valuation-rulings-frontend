/*
 * Copyright 2025 HM Revenue & Customs
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
import cats.data.Validated.Valid
import connectors.BackendConnector
import models.AuthUserType.IndividualTrader
import models._
import models.requests._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, TryValues}
import pages._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import services.checkAnswers.ApplicationSummaryService
import userrole.{UserRole, UserRoleProvider}
import viewmodels.checkAnswers.summary.ApplicationSummary

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val mockSubmissionService = mock(classOf[SubmissionService])

  override def beforeEach(): Unit = {
    reset(mockSubmissionService)
    super.beforeEach()
  }

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET for a UserRole with the agent creds flag on" in new CheckYourAnswersControllerSpecSetup {

      private val mockUserRoleProvider = mock(classOf[UserRoleProvider])
      private val mockUserRole         = mock(classOf[UserRole])

      private val expectedText = "Expected"
      private val expectedView = HtmlFormat.raw(expectedText)

      when(mockUserRoleProvider.getUserRole(any())).thenReturn(mockUserRole)
      when(
        mockUserRole.selectViewForCheckYourAnswers(any[ApplicationSummary], any[DraftId])(
          any[DataRequest[AnyContent]],
          any[Messages]
        )
      )
        .thenReturn(expectedView)

      private val application = applicationBuilder(userAnswers = Option(userAnswers))
        .overrides(
          bind[BackendConnector].toInstance(mockBackendConnector),
          bind[ApplicationRequestService].toInstance(mockApplicationRequestService),
          bind[ApplicationSummaryService].toInstance(mockApplicationSummaryService),
          bind[UserRoleProvider].toInstance(mockUserRoleProvider)
        )
        .build()

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

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(draftId).url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedText
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in
      new CheckYourAnswersControllerSpecSetup {

        private val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    "must redirect to Application Complete when application submission succeeds" in
      new CheckYourAnswersControllerSpecSetup {
        private val applicationId = ApplicationId(1)
        private val response      = ApplicationSubmissionResponse(applicationId)

        when(this.mockSubmissionService.submitApplication(any(), any())(any()))
          .thenReturn(Future.successful(response))
        when(mockBackendConnector.getTraderDetails(any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(traderDetailsWithCountryCode)))

        private val application = applicationBuilder(Option(fullUserAnswers))
          .overrides(
            bind[SubmissionService].toInstance(this.mockSubmissionService),
            bind[BackendConnector].toInstance(mockBackendConnector),
            bind[ApplicationSummaryService].toInstance(mockApplicationSummaryService)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ApplicationCompleteController
            .onPageLoad(applicationId.toString)
            .url
        }
      }
  }

  "must redirect to journey Recovery when unable to fetch trader details" in
    new CheckYourAnswersControllerSpecSetup {
      private val applicationId = ApplicationId(1)
      private val response      = ApplicationSubmissionResponse(applicationId)

      when(this.mockSubmissionService.submitApplication(any(), any())(any()))
        .thenReturn(Future.successful(response))
      when(mockBackendConnector.getTraderDetails(any(), any())(any(), any()))
        .thenReturn(Future.successful(Left(BackendError(500, "error"))))

      private val application = applicationBuilderAsOrg(Option(fullUserAnswers))
        .overrides(
          bind[SubmissionService].toInstance(this.mockSubmissionService),
          bind[BackendConnector].toInstance(mockBackendConnector)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(draftId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController
          .onPageLoad()
          .url
      }
    }
}

trait CheckYourAnswersControllerSpecSetup extends TryValues with ScalaFutures {
  val userAnswersId: String    = "id"
  val DraftIdSequence          = 123456789L
  val draftId: DraftId         = DraftId(DraftIdSequence)
  val userAnswers: UserAnswers = UserAnswers(userAnswersId, draftId)
    .setFuture(AccountHomePage, IndividualTrader)
    .futureValue

  val mockSubmissionService: SubmissionService                 = mock(classOf[SubmissionService])
  val mockBackendConnector: BackendConnector                   = mock(classOf[BackendConnector])
  val mockApplicationSummaryService: ApplicationSummaryService = mock(classOf[ApplicationSummaryService])
  val mockApplicationRequestService: ApplicationRequestService = mock(classOf[ApplicationRequestService])

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

  val appSummary: ApplicationSummary            = mock(classOf[ApplicationSummary])
  val appApplicationRequest: ApplicationRequest = mock(classOf[ApplicationRequest])

  when(
    mockApplicationSummaryService.getApplicationSummary(
      any[UserAnswers],
      any[TraderDetailsWithCountryCode]
    )(any[Messages])
  ).thenReturn(appSummary)

  when(
    mockApplicationRequestService.apply(
      any[UserAnswers],
      any[TraderDetailsWithCountryCode]
    )
  ).thenReturn(Valid(appApplicationRequest))

  val fullUserAnswers: UserAnswers = (for {
    ua <- userAnswers.set(DescriptionOfGoodsPage, "DescriptionOfGoodsPage")
    ua <- ua.set(HasCommodityCodePage, false)
    ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
    ua <- ua.set(HasConfidentialInformationPage, false)
    ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)
    ua <- ua.set(
            CheckRegisteredDetailsPage,
            true
          )
    ua <- ua.set(
            ApplicationContactDetailsPage,
            ApplicationContactDetails(
              name = "name",
              email = "email",
              phone = "phone",
              jobTitle = "jobTitle"
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
