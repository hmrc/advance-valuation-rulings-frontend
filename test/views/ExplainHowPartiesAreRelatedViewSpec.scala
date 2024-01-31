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

import forms.ExplainHowPartiesAreRelatedFormProvider
import models.{DraftId, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.ExplainHowPartiesAreRelatedView

class ExplainHowPartiesAreRelatedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "explainHowPartiesAreRelated"

  val form: ExplainHowPartiesAreRelatedFormProvider = app.injector.instanceOf[ExplainHowPartiesAreRelatedFormProvider]

  val view: ExplainHowPartiesAreRelatedView = app.injector.instanceOf[ExplainHowPartiesAreRelatedView]

  val viewViaApply: HtmlFormat.Appendable  = view(form.apply(), NormalMode, DraftId(1L))(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(form.apply(), NormalMode, DraftId(1L), fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(form.apply(), NormalMode, DraftId(1L))(fakeRequest, messages)

  "ExplainHowPartiesAreRelatedView" - {
    normalPage(messageKeyPrefix, "")()
  }
}
