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

import base.SpecBase
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase {

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the answers alive and return OK" in {

        val mockUserAnswersService = mock[UserAnswersService]

        when(mockUserAnswersService.keepAlive(any())(any())) thenReturn Future.successful(
          Done
        )

        val application =
          applicationBuilder(Some(userAnswersAsIndividualTrader))
            .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockUserAnswersService, times(1))
            .keepAlive(eqTo(draftId))(any())
        }
      }
    }

    "when the user has not answered any questions" - {

      "must return OK" in {

        val mockUserAnswersService = mock[UserAnswersService]
        when(mockUserAnswersService.keepAlive(any())(any())) thenReturn Future.successful(
          Done
        )

        val application =
          applicationBuilder(None)
            .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive(draftId).url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockUserAnswersService, never).keepAlive(any())(any())
        }
      }
    }
  }
}
