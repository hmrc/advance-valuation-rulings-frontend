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

package views

import models.AuthUserType.{IndividualTrader, OrganisationUser}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.EORIBeUpToDateView

class EORIBeUpToDateViewSpec extends ViewBehaviours {

  private val view: EORIBeUpToDateView = app.injector.instanceOf[EORIBeUpToDateView]

  val viewViaApply: HtmlFormat.Appendable  = view.apply(draftId, IndividualTrader)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(draftId, IndividualTrader, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(draftId, IndividualTrader)(fakeRequest, messages)

  "EORIBeUpToDateView" - {
    normalPage("eoriBeUpToDate")()

    "when not an Individual Trader" - {
      val viewAlternate: HtmlFormat.Appendable = view.apply(draftId, OrganisationUser)(fakeRequest, messages)

      renderPage(viewAlternate, "eoriBeUpToDate", Some("org"))()
    }
  }
}
