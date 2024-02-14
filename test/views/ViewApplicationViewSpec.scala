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

import generators.ApplicationGenerator
import models.requests._
import play.twirl.api.HtmlFormat
import viewmodels.ApplicationViewModel
import views.behaviours.ViewBehaviours
import views.html.ViewApplicationView

import java.time.Instant

class ViewApplicationViewSpec extends ViewBehaviours with ApplicationGenerator {

  private val application: Application = arbitraryApplication.arbitrary.sample.value

  private val view: ViewApplicationView = app.injector.instanceOf[ViewApplicationView]

  val viewViaApply: HtmlFormat.Appendable  = view.apply(
    ApplicationViewModel(application),
    ApplicationId(1L).toString,
    Instant.now.toString
  )(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(
    ApplicationViewModel(application),
    ApplicationId(1L).toString,
    Instant.now.toString,
    fakeRequest,
    messages
  )
  val viewViaF: HtmlFormat.Appendable      =
    view.f(ApplicationViewModel(application), ApplicationId(1L).toString, Instant.now.toString)(fakeRequest, messages)

  "ViewApplicationView" - {
    normalPage("viewApplication")()
  }
}
