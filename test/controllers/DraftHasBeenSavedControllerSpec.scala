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
import models.{DraftHasBeenSavedModel, DraftId, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.DraftHasBeenSavedView

import java.time.{Clock, Instant, ZoneId}

class DraftHasBeenSavedControllerSpec extends SpecBase {

  val fixedInstant: Instant = Instant.parse("2023-04-05T00:00:00Z")
  val fixedClock: Clock     =
    Clock.fixed(fixedInstant, ZoneId.of("Europe/London"))
  val date: String          = DraftHasBeenSavedModel().get28DaysLater(Instant.now(fixedClock))

  "DraftHasBeenSaved Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers(userAnswersId, draftId, lastUpdated = fixedInstant)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.DraftHasBeenSavedController.onPageLoad(DraftId(12345)).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DraftHasBeenSavedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(date)(
          request,
          messages(application)
        ).toString
      }
    }

  }
}
