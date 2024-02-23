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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.AgentForTraderCheckYourAnswersView

class AgentForTraderCheckYourAnswersViewSpec extends ViewBehaviours {

  private val view: AgentForTraderCheckYourAnswersView = app.injector.instanceOf[AgentForTraderCheckYourAnswersView]

  val viewViaApply: HtmlFormat.Appendable  = view.apply(testApplicationSummary, draftId)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(testApplicationSummary, draftId, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(testApplicationSummary, draftId)(fakeRequest, messages)

  "AgentForTraderCheckYourAnswersView" - {
    normalPage("agentForTraderCheckYourAnswers")()
  }
}
