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
import controllers.actions._
import forms.ChangeYourRoleImporterFormProvider
import models.{Done, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.mockito.stubbing.OngoingStubbing
import pages.ChangeYourRoleImporterPage
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import views.html.ChangeYourRoleImporterView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ChangeYourRoleImporterControllerSpec extends SpecBase {

  private val mockUserAnswersService: UserAnswersService = mock(classOf[UserAnswersService])
  private val dataRetrievalActionProvider                = new DataRetrievalActionProvider(mockUserAnswersService)

  private val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[DataRequiredAction].to[DataRequiredActionImpl],
      bind[IdentifierAction].to[FakeIdentifierAction],
      bind[UserAnswersService].to(mockUserAnswersService),
      bind[DataRetrievalActionProvider].toInstance(dataRetrievalActionProvider)
    )
    .configure(configurationBuilder)
    .build()

  private val changeYourRoleImporterView: ChangeYourRoleImporterView                 =
    app.injector.instanceOf[ChangeYourRoleImporterView]
  private val changeYourRoleImporterFormProvider: ChangeYourRoleImporterFormProvider =
    app.injector.instanceOf[ChangeYourRoleImporterFormProvider]

  private def mockGetAnswers(result: Option[UserAnswers]): OngoingStubbing[Future[Option[UserAnswers]]] =
    when(mockUserAnswersService.get(any())(any())).thenReturn(Future.successful(result))

  private def mockSetAnswers(): OngoingStubbing[Future[Done]] =
    when(mockUserAnswersService.set(any())(any())).thenReturn(Future.successful(Done))

  private def mockClearAnswers(): OngoingStubbing[Future[Done]] =
    when(mockUserAnswersService.clear(any())(any())).thenReturn(Future.successful(Done))

  override def beforeEach(): Unit =
    reset(mockUserAnswersService)

  "ChangeYourRoleImporterController" - {
    ".onPageLoad" - {
      "must return OK and the correct view for a GET" in {

        mockGetAnswers(Some(emptyUserAnswers))

        val router: Call                                 = controllers.routes.ChangeYourRoleImporterController.onPageLoad(NormalMode, draftId)
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, router.url)

        val result: Future[Result] = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual changeYourRoleImporterView(
          changeYourRoleImporterFormProvider(),
          draftId,
          controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
        )(request, messagesApi(app).preferred(request)).toString
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers: UserAnswers = emptyUserAnswers.set(ChangeYourRoleImporterPage, true).success.value

        mockGetAnswers(Some(userAnswers))

        val router: Call                                 = controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
        val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, router.url)

        val result: Future[Result] = route(app, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual changeYourRoleImporterView(
          changeYourRoleImporterFormProvider().fill(true),
          draftId,
          router
        )(request, messagesApi(app).preferred(request)).toString
      }
    }

    ".onSubmit" - {
      "in Normal Mode" - {
        "user answers Yes" - {
          "must redirect to the next page when valid data is submitted" in {

            mockGetAnswers(Some(emptyUserAnswers))
            mockClearAnswers()
            mockSetAnswers()

            val router: Call                                     = controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
            val request: FakeRequest[AnyContentAsFormUrlEncoded] =
              FakeRequest(POST, router.url).withFormUrlEncodedBody(("value", "true"))

            val result: Future[Result] = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).get mustEqual controllers.routes.WhatIsYourRoleAsImporterController
              .onPageLoad(NormalMode, draftId)
              .url
          }
        }
      }

      "user answers No" - {
        "must redirect to the next page when valid data is submitted" in {

          mockGetAnswers(Some(emptyUserAnswers))
          mockSetAnswers()

          val router: Call                                     = controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
          val request: FakeRequest[AnyContentAsFormUrlEncoded] =
            FakeRequest(POST, router.url).withFormUrlEncodedBody(("value", "false"))

          val result: Future[Result] = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).get mustEqual controllers.routes.WhatIsYourRoleAsImporterController
            .onPageLoad(NormalMode, draftId)
            .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        mockGetAnswers(Some(emptyUserAnswers))
        mockSetAnswers()

        val router: Call                                     = controllers.routes.ChangeYourRoleImporterController.onSubmit(NormalMode, draftId)
        val request: FakeRequest[AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, router.url).withFormUrlEncodedBody(("value", "1"))

        val result: Future[Result] = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }

    }
  }
}
