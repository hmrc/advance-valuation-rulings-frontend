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

import base.SpecBase
import models.{Done, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ConfidentialInformationPage
import services.UserAnswersService

trait SaveDraftSpec extends SpecBase with MockitoSugar {
  lazy val saveDraftRoute: String =
    routes.ExplainReasonComputedValueController.onSubmit(NormalMode, draftId, saveDraft = true).url

  // Cannot pass through the route, it will error a 400

  "Redirects to Draft saved page when save-draft is selected" in {

    val mockUserAnswersService = mock[UserAnswersService]

    when(mockUserAnswersService.set(any())(any())) thenReturn Future.successful(Done)

    val application =
      applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader))
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

    running(application) {
      val request =
        FakeRequest(POST, saveDraftRoute)
          .withFormUrlEncodedBody(("value", "answer"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual Call(
        "POST",
        s"/advance-valuation-ruling/$draftId/save-as-draft"
      ).url
    }
  }

}
