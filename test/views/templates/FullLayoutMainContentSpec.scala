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

package views.templates

import play.twirl.api.{Html, HtmlFormat}
import views.behaviours.ViewBehaviours
import views.html.templates.FullLayoutMainContent

class FullLayoutMainContentSpec extends ViewBehaviours {

  private val view: FullLayoutMainContent = app.injector.instanceOf[FullLayoutMainContent]

  val viewViaApply: HtmlFormat.Appendable  = view.apply(Html("<p>Html Content</p>"))
  val viewViaRender: HtmlFormat.Appendable = view.render(Html("<p>Html Content</p>"))
  val viewViaF: HtmlFormat.Appendable      = view.f(Html("<p>Html Content</p>"))

  "FullLayoutMainContent" - {
    "must display Html content" in {
      val doc       = asDocument(viewViaApply)
      val paragraph = doc.getElementsByTag("p")
      paragraph.text mustBe "Html Content"
    }
  }
}
