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
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier

import base.SpecBase
import connectors.BackendConnector
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import pages._
import viewmodels.checkAnswers.summary.ApplicationSummary
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersForAgentsView

class CheckYourAnswersForAgentsControllerSpec extends SpecBase with SummaryListFluency {

  implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val userAnswers = emptyUserAnswers

  "Check Your Answers for Agents Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilderAsOrg(userAnswers = Option(userAnswers)).build()

      implicit val msgs: Messages = messages(application)

      running(application) {
        implicit val request =
          FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersForAgentsView]
        val list = ApplicationSummary(userAnswers, AffinityGroup.Organisation)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilderAsOrg(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersForAgentsController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Application Complete when application submission succeeds" in {
      val answers = (for {
        ua <- emptyUserAnswers.set(DescriptionOfGoodsPage, "DescriptionOfGoodsPage")
        ua <- ua.set(HasCommodityCodePage, false)
        ua <- ua.set(HaveTheGoodsBeenSubjectToLegalChallengesPage, false)
        ua <- ua.set(HasConfidentialInformationPage, false)
        ua <- ua.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg)
        ua <- ua.set(
                CheckRegisteredDetailsPage,
                CheckRegisteredDetails(
                  value = true,
                  eori = "eori",
                  name = "name",
                  streetAndNumber = "streetAndNumber",
                  city = "city",
                  country = "country",
                  postalCode = Some("postalCode")
                )
              )
        ua <- ua.set(
                BusinessContactDetailsPage,
                BusinessContactDetails(
                  name = "name",
                  email = "email",
                  phone = "phone",
                  company = "company"
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

      val application = applicationBuilderAsOrg(Option(answers))
        .build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersForAgentsController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ApplicationCompleteController
          .onPageLoad(models.requests.ApplicationId(0L).toString)
          .url
      }
    }

    "must redirect to Journey Recovery when application submission fails" in {

      val mockBackendConnector = mock[BackendConnector]
      val application          = applicationBuilderAsOrg(Option(userAnswers))
        .overrides(bind[BackendConnector].to(mockBackendConnector))
        .build()

      when(
        mockBackendConnector.submitAnswers(any())(any(), any())
      ) thenReturn Future.successful(Left(BackendError(INTERNAL_SERVER_ERROR, "backend error")))

      running(application) {
        val request = FakeRequest(POST, routes.CheckYourAnswersForAgentsController.onSubmit.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}