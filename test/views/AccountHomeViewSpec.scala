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

import models.ApplicationForAccountHome
import models.requests.{ApplicationId, ApplicationSummary}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AccountHomeView

import java.time.Instant

class AccountHomeViewSpec extends ViewBehaviours {

  private val applications: Seq[ApplicationForAccountHome] = Seq(
    ApplicationForAccountHome(ApplicationSummary(ApplicationId(1L), "GoodsDescription", Instant.now(), EoriNumber))
  )

  private val view: AccountHomeView = app.injector.instanceOf[AccountHomeView]

  val viewViaApply: HtmlFormat.Appendable  = view.apply(applications)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(applications, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(applications)(fakeRequest, messages)

  "AccountHomeView" - {
    normalPage("accountHome")()

    "when there are no registered applications" - {

      val viewAlternate: HtmlFormat.Appendable = view.apply(Seq.empty)(fakeRequest, messages)

      renderPageWithAssertions(viewAlternate, "accountHome")() {
        "prompt that no applications have been started" in {
          assertContainsMessages(asDocument(viewAlternate), messages("accountHome.para"))
        }
      }
    }
  }
}
