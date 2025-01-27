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

import forms.BusinessContactDetailsFormProvider
import models.NormalMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.BusinessContactDetailsView

class BusinessContactDetailsViewSpec extends ViewBehaviours {

  private val form: BusinessContactDetailsFormProvider = app.injector.instanceOf[BusinessContactDetailsFormProvider]

  private val view: BusinessContactDetailsView = app.injector.instanceOf[BusinessContactDetailsView]

  val viewViaApply: HtmlFormat.Appendable  =
    view.apply(form.apply(false), NormalMode, draftId, includeCompanyName = false)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(form.apply(false), NormalMode, draftId, includeCompanyName = false, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      = view.f(form.apply(false), NormalMode, draftId, false)(fakeRequest, messages)

  "BusinessContactDetailsView" - {
    normalPage("businessContactDetails")()

    "when an agent acts on behalf of a trader" - {
      val viewAlternate: HtmlFormat.Appendable =
        view.apply(form.apply(true), NormalMode, draftId, includeCompanyName = true)(fakeRequest, messages)

      renderPageWithAssertions(viewAlternate, "businessContactDetails")() {
        "display a paragraph informing how the details will be used" in {
          assertContainsMessages(asDocument(viewAlternate), messages("businessContactDetails.paragraph.agentTrader"))
        }
        "display an additional input field to enter the businesses name" in {
          assertContainsLabel(asDocument(viewAlternate), "companyName", "Registered business name")
          assertElementHasClass(asDocument(viewAlternate), "companyName", "govuk-input")
        }
      }
    }
  }
}
