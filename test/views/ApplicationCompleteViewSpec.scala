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

package views

import models.requests.ApplicationId
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.ApplicationCompleteView

class ApplicationCompleteViewSpec extends ViewBehaviours {

  private val view: ApplicationCompleteView = app.injector.instanceOf[ApplicationCompleteView]

  val viewViaApply: HtmlFormat.Appendable  =
    view.apply(isIndividual = true, ApplicationId(1L).toString, ContactEmail)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(isIndividual = true, ApplicationId(1L).toString, ContactEmail, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(true, ApplicationId(1L).toString, ContactEmail)(fakeRequest, messages)

  "ApplicationCompleteView" - {
    normalPage("applicationComplete")()
  }
}
