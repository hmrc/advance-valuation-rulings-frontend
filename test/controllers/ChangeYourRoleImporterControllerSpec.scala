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
import connectors.BackendConnector
import controllers.actions.{DataRetrievalActionProvider, FakeIdentifierAction}
import forms.ChangeYourRoleImporterForm
import models.requests.DataRequest
import models.{Done, NormalMode, UserAnswers}
import navigation.FakeNavigators.FakeNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import pages.ChangeYourRoleImporterPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DeleteAllUserAnswersService, UserAnswersService}
import uk.gov.hmrc.auth.core.{AffinityGroup, User}
import views.html.ChangeYourRoleImporterView

import scala.concurrent.{ExecutionContext, Future}

class ChangeYourRoleImporterControllerSpec extends SpecBase {

  object FakeIdentifierAction extends FakeIdentifierAction(playBodyParsers)

  val changeYourRoleImporterView                             = injector.instanceOf[ChangeYourRoleImporterView]
  val changeYourRoleImporterForm: ChangeYourRoleImporterForm = new ChangeYourRoleImporterForm()

  implicit lazy val ec: ExecutionContext = injector.instanceOf[ExecutionContext]

  val useranswers: UserAnswers =
    userAnswersAsIndividualTrader

  val fakeBackendConnector = new BackendConnector(frontendAppConfig, httpV2)

  val mockUserAnswersService = mock[UserAnswersService]

  val fakeDataRetrievalAction = new DataRetrievalActionProvider(mockUserAnswersService)

  val deleteAllUserAnswersService = new DeleteAllUserAnswersService

  def mockGetAnswers(result: Option[UserAnswers]): Unit =
    when(mockUserAnswersService.get(any())(any()))
      .thenReturn(Future.successful(result))

  def mockSetAnswers(): Unit =
    when(mockUserAnswersService.set(any())(any()))
      .thenReturn(Future.successful(Done))

  def mockClearAnswers(): Unit =
    when(mockUserAnswersService.clear(any())(any()))
      .thenReturn(Future.successful(Done))

  val fakeDataRequest: DataRequest[_] = DataRequest(
    request = FakeRequest(),
    userId = userAnswersId,
    eoriNumber = EoriNumber,
    affinityGroup = AffinityGroup.Individual,
    credentialRole = Option(User),
    userAnswers = userAnswersAsIndividualTrader
  )

  val controller: ChangeYourRoleImporterController =
    new ChangeYourRoleImporterController(
      messagesApi = messagesApi,
      controllerComponents = messagesControllerComponents,
      navigator = new FakeNavigator(onwardRoute),
      identify = FakeIdentifierAction,
      requireData = dataRequiredAction,
      getData = fakeDataRetrievalAction,
      formProvider = changeYourRoleImporterForm,
      deleteAllUserAnswersService = deleteAllUserAnswersService,
      view = changeYourRoleImporterView,
      userAnswersService = mockUserAnswersService,
      backendConnector = fakeBackendConnector
    )

  "ChangeYourRoleImporterController" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val userAnswers: UserAnswers =
          emptyUserAnswers

        mockGetAnswers(Some(userAnswers))

        val route   = controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
        val request = FakeRequest(GET, route.url)
        val result  = controller.onPageLoad(NormalMode, draftId)(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual changeYourRoleImporterView(
          changeYourRoleImporterForm(),
          draftId,
          route
        )(
          request,
          messagesHelper(request)
        ).toString
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers: UserAnswers =
          emptyUserAnswers
            .set(ChangeYourRoleImporterPage, true)
            .success
            .value

        mockGetAnswers(Some(userAnswers))

        val route   = controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
        val request = FakeRequest(GET, route.url)

        val result = controller.onPageLoad(NormalMode, draftId)(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual changeYourRoleImporterView(
          changeYourRoleImporterForm().fill(true),
          draftId,
          route
        )(
          request,
          messagesHelper(request)
        ).toString
      }
    }

    ".onSubmit" - {

      "in Normal Mode" - {

        "user answers Yes" - {

          "must redirect to the next page when valid data is submitted" in {

            val userAnswers: UserAnswers =
              emptyUserAnswers

            mockGetAnswers(Some(userAnswers))
            mockSetAnswers()
            mockClearAnswers()

            val request = FakeRequest("", "").withFormUrlEncodedBody(("value", "true"))

            val result = controller.onSubmit(NormalMode, draftId)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some("/foo")
          }
        }

        "user answers No" - {

          "must redirect to the next page when valid data is submitted" in {

            val userAnswers: UserAnswers =
              emptyUserAnswers

            mockGetAnswers(Some(userAnswers))
            mockSetAnswers()

            val request = FakeRequest("", "").withFormUrlEncodedBody(("value", "false"))

            val result = controller.onSubmit(NormalMode, draftId)(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustEqual Some("/foo")
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val userAnswers: UserAnswers =
            emptyUserAnswers

          mockGetAnswers(Some(userAnswers))
          mockSetAnswers()

          val request = FakeRequest("", "").withFormUrlEncodedBody(("value", "1"))

          val result = controller.onSubmit(NormalMode, draftId)(request)

          status(result) mustEqual BAD_REQUEST
        }
      }
    }
  }
}
