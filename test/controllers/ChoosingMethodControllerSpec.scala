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

import play.api.test.FakeRequest
import play.api.test.Helpers._

import base.SpecBase
import forms.ValuationMethodFormProvider
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import views.html.ChoosingMethodView

class ChoosingMethodControllerSpec extends SpecBase with MockitoSugar {

  lazy val choosingMethodRoute =
    routes.ChoosingMethodController.onPageLoad(draftId).url

  val formProvider = new ValuationMethodFormProvider()
  val form         = formProvider()

  "ChoosingMethod Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

      running(application) {
        val request = FakeRequest(GET, choosingMethodRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChoosingMethodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, draftId)(
          request,
          messages(application)
        ).toString
      }
    }
  }
}
