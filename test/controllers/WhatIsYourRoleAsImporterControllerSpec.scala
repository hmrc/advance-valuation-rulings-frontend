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

import audit.AuditService
import base.SpecBase
import forms.WhatIsYourRoleAsImporterFormProvider
import models.AuthUserType.IndividualTrader
import models.WhatIsYourRoleAsImporter.EmployeeOfOrg
import models._
import navigation.FakeNavigators.FakeNavigator
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import pages.{AccountHomePage, AgentCompanyDetailsPage, WhatIsYourRoleAsImporterPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.WhatIsYourRoleAsImporterView

import scala.concurrent.Future

class WhatIsYourRoleAsImporterControllerSpec extends SpecBase {

  private val mockAuditService: AuditService             = mock(classOf[AuditService])
  private val mockUserAnswersService: UserAnswersService = mock(classOf[UserAnswersService])

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditService)
    reset(mockUserAnswersService)
  }

  private lazy val whatIsYourRoleAsImporterRoute =
    routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId).url

  private val formProvider = new WhatIsYourRoleAsImporterFormProvider()
  private val form         = formProvider()

  "WhatIsYourRoleAsImporter Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilderAsAgent(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[AuditService].to(mockAuditService))
        .build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourRoleAsImporterRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsYourRoleAsImporterView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersAsIndividualTrader
        .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.values.head)
        .success
        .value

      val application = applicationBuilderAsAgent(userAnswers = Some(userAnswers))
        .overrides(bind[AuditService].to(mockAuditService))
        .build()

      running(application) {
        val request = FakeRequest(GET, whatIsYourRoleAsImporterRoute)

        val view = application.injector.instanceOf[WhatIsYourRoleAsImporterView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form = form.fill(WhatIsYourRoleAsImporter.values.head),
            mode = NormalMode,
            draftId = draftId
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val userAnswers = userAnswersAsIndividualTrader
        .set(
          AgentCompanyDetailsPage,
          AgentCompanyDetails(
            agentEori = "agentEori",
            agentCompanyName = "agentCompanyName",
            agentStreetAndNumber = "agentStreetAndNumber",
            agentCity = "agentCity",
            agentCountry = Country("GB", "United Kingdom"),
            agentPostalCode = Some("agentPostalCode")
          )
        )
        .success
        .value

      val application =
        applicationBuilderAsAgent(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[AuditService].to(mockAuditService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

      val expectedUserAnswers =
        userAnswers
          .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
          .success
          .value

      verify(mockUserAnswersService, times(1)).set(eqTo(expectedUserAnswers))(any())
    }

    "must remove answer for AgentCompanyDetails when answered as Employee" in {

      val emptyAnswers =
        UserAnswers(userAnswersId, draftId)
          .set(AccountHomePage, AuthUserType.OrganisationUser)
          .success
          .value

      when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

      val userAnswers = emptyAnswers
        .set(
          AgentCompanyDetailsPage,
          AgentCompanyDetails(
            agentEori = "agentEori",
            agentCompanyName = "agentCompanyName",
            agentStreetAndNumber = "agentStreetAndNumber",
            agentCity = "agentCity",
            agentCountry = Country("GB", "United Kingdom"),
            agentPostalCode = Some("agentPostalCode")
          )
        )
        .success
        .value

      val application =
        applicationBuilderAsAgent(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[AuditService].to(mockAuditService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.EmployeeOfOrg.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilderAsAgent(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(bind[AuditService].to(mockAuditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhatIsYourRoleAsImporterView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }

      verify(mockAuditService, times(0)).sendRoleIndicatorEvent(any())(any(), any(), any())
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[AuditService].to(mockAuditService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, whatIsYourRoleAsImporterRoute)
            .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }

      verify(mockAuditService, times(0)).sendRoleIndicatorEvent(any())(any(), any(), any())
    }

    ".onSubmitNavigationLogic" - {

      "NormalMode" - {

        "when the user enters any role and there is no previously answered role stored in request.useranswers" - {

          "redirect to the RequiredInformation page" in {

            when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

            val userAnswers =
              emptyUserAnswers
                .set(AccountHomePage, IndividualTrader)
                .success
                .value

            val application =
              applicationBuilderAsAgent(userAnswers = Some(userAnswers))
                .overrides(
                  bind[UserAnswersService].toInstance(mockUserAnswersService),
                  bind[AuditService].to(mockAuditService)
                )
                .build()

            val routeToTest =
              routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId).url

            running(application) {
              val request =
                FakeRequest(POST, routeToTest)
                  .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.EmployeeOfOrg.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.RequiredInformationController
                .onPageLoad(draftId)
                .url
            }
          }
        }

        "when the user enters any role == a previously answered role stored in request.useranswers" - {

          "redirect to the RequiredInformation page" in {

            when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

            val userAnswers =
              emptyUserAnswers
                .set(AccountHomePage, IndividualTrader)
                .success
                .value
                .set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
                .success
                .value

            val application =
              applicationBuilderAsAgent(userAnswers = Some(userAnswers))
                .overrides(
                  bind[UserAnswersService].toInstance(mockUserAnswersService),
                  bind[AuditService].to(mockAuditService)
                )
                .build()

            val routeToTest =
              routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId).url

            running(application) {
              val request =
                FakeRequest(POST, routeToTest)
                  .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.EmployeeOfOrg.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.RequiredInformationController
                .onPageLoad(draftId)
                .url
            }
          }
        }

        "when the user enters any role != a previously answered role stored in request.useranswers" - {

          "redirect to the RequiredInformation page" in {

            when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

            val userAnswers =
              emptyUserAnswers
                .set(AccountHomePage, IndividualTrader)
                .success
                .value
                .set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
                .success
                .value

            val application =
              applicationBuilderAsAgent(userAnswers = Some(userAnswers))
                .overrides(
                  bind[UserAnswersService].toInstance(mockUserAnswersService),
                  bind[AuditService].to(mockAuditService)
                )
                .build()

            val routeToTest =
              routes.WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, draftId).url

            running(application) {
              val request =
                FakeRequest(POST, routeToTest)
                  .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.ChangeYourRoleImporterController
                .onPageLoad(NormalMode, draftId)
                .url
            }
          }
        }
      }

      "CheckMode" - {

        "when the user enters a role == role stored in request.useranswers" - {

          "redirect to the CheckYourAnswers page" in {

            when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

            val userAnswers =
              emptyUserAnswers
                .set(AccountHomePage, IndividualTrader)
                .success
                .value
                .set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
                .success
                .value

            val application =
              applicationBuilderAsAgent(userAnswers = Some(userAnswers))
                .overrides(
                  bind[UserAnswersService].toInstance(mockUserAnswersService),
                  bind[AuditService].to(mockAuditService)
                )
                .build()

            val routeToTest =
              routes.WhatIsYourRoleAsImporterController.onPageLoad(CheckMode, draftId).url

            running(application) {
              val request =
                FakeRequest(POST, routeToTest)
                  .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.EmployeeOfOrg.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.CheckYourAnswersController
                .onPageLoad(draftId)
                .url
            }
          }
        }

        "when the user enters any role != a previously answered role stored in request.useranswers" - {

          "redirect to the RequiredInformation page" in {

            when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

            val userAnswers =
              emptyUserAnswers
                .set(AccountHomePage, IndividualTrader)
                .success
                .value
                .set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
                .success
                .value

            val application =
              applicationBuilderAsAgent(userAnswers = Some(userAnswers))
                .overrides(
                  bind[UserAnswersService].toInstance(mockUserAnswersService),
                  bind[AuditService].to(mockAuditService)
                )
                .build()

            val routeToTest =
              routes.WhatIsYourRoleAsImporterController.onPageLoad(CheckMode, draftId).url

            running(application) {
              val request =
                FakeRequest(POST, routeToTest)
                  .withFormUrlEncodedBody(("value", WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.ChangeYourRoleImporterController
                .onPageLoad(CheckMode, draftId)
                .url
            }
          }
        }
      }
    }
  }
}
