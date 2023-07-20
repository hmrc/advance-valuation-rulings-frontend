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
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat

import base.SpecBase
import models.{Done, UserAnswers}
import models.requests.DataRequest
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import services.UserAnswersService
import userrole.{UserRole, UserRoleProvider}

class EORIBeUpToDateControllerSpec extends SpecBase with MockitoSugar {

  private lazy val checkRegisteredDetailRoute =
    routes.CheckRegisteredDetailsController.onPageLoad(models.NormalMode, draftId).url

  private lazy val eoriBeUpToDateRoute =
    routes.EORIBeUpToDateController.onPageLoad(draftId).url

  private val mockUserRoleProvider = mock[UserRoleProvider]
  private val mockUserRole = mock[UserRole]
  private val mockUserAnswersService = mock[UserAnswersService]

  private def setUpUserAnswersServiceMock(answers: UserAnswers) = {
    when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)
    when(mockUserAnswersService.get(any())(any())) thenReturn Future.successful(
      Some(userAnswersAsIndividualTrader)
    )
  }

  private def setUpUserRoleProviderMock() = {
    when(mockUserRoleProvider.getUserRole(any[UserAnswers]))
      .thenReturn(mockUserRole)
  }

  private def setUpViewMockForUserRole(expectedViewBody: String = "") = {
    val expectedView = HtmlFormat.raw(expectedViewBody)
    when(
      mockUserRole.selectViewForEoriBeUpToDate(
        ArgumentMatchers.eq(this.draftId)
      )(any[DataRequest[AnyContent]], any[Messages])
    ).thenReturn(expectedView)

    when(mockUserRoleProvider.getUserRole(any())).thenReturn(mockUserRole)
  }

  "EORIBeUpToDate Controller" - {

    setUpUserRoleProviderMock()
    setUpViewMockForUserRole()
    setUpUserAnswersServiceMock(userAnswersAsIndividualTrader)
    setUpViewMockForUserRole()

    "must return OK for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.EORIBeUpToDateController.onPageLoad(draftId).url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to the next page when yes is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[UserRoleProvider].toInstance(mockUserRoleProvider)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }

    }

    "must redirect to the next page when 'no' is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkRegisteredDetailRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must show the view given by the UserRole" in {

      val expectedViewBody = "hello"
      setUpViewMockForUserRole(expectedViewBody)

      val application = applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[UserRoleProvider].toInstance(mockUserRoleProvider)
        )
        .build()

      running(application) {
        val request        = FakeRequest(GET, eoriBeUpToDateRoute)
        val result         = route(application, request).value
        val actualViewBody = contentAsString(result)

        actualViewBody mustBe expectedViewBody
      }

    }

  }

}
