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

import forms.VerifyTraderDetailsFormProvider
import models.{DraftId, NormalMode, TraderDetailsWithConfirmation}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.VerifyPublicTraderDetailView

class VerifyPublicTraderDetailViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "checkRegisteredDetails"

  val role: TraderDetailsWithConfirmation = TraderDetailsWithConfirmation(traderDetailsWithCountryCode)

  val doesNotConsentToDisclosureOfPersonalData: TraderDetailsWithConfirmation =
    role.copy(consentToDisclosureOfPersonalData = false)

  val emptyCDSFullName: TraderDetailsWithConfirmation =
    role.copy(CDSFullName = "")

  val form: VerifyTraderDetailsFormProvider = app.injector.instanceOf[VerifyTraderDetailsFormProvider]

  val view: VerifyPublicTraderDetailView = app.injector.instanceOf[VerifyPublicTraderDetailView]

  val viewViaApply: HtmlFormat.Appendable  =
    view(form.apply(Some(role)), NormalMode, DraftId(1L), role)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(form.apply(Some(role)), NormalMode, DraftId(1L), role, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(form.apply(Some(role)), NormalMode, DraftId(1L), role)(fakeRequest, messages)

  "VerifyPublicTraderDetailView" - {
    normalPage(messageKeyPrefix, "agentTrader")()
  }

  "the trader does not consent to disclosure of personal data" - {
    val viewAlternate =
      view(
        form.apply(Some(doesNotConsentToDisclosureOfPersonalData)),
        NormalMode,
        DraftId(1L),
        doesNotConsentToDisclosureOfPersonalData
      )(fakeRequest, messages)

    pageByMethodWithAssertions(
      viewAlternate,
      s"$messageKeyPrefix.private",
      "",
      doesNotConsentToDisclosureOfPersonalData.EORINo
    )() {
      "contains the correct advisory paragraph" in {
        assertContainsMessages(asDocument(viewAlternate), messages("checkRegisteredDetails.private.paragraph.1"))
      }
    }
  }

  "when CDSFullName is an empty string" - {
    val viewAlternate =
      view(form.apply(Some(emptyCDSFullName)), NormalMode, DraftId(1L), emptyCDSFullName)(
        fakeRequest,
        messages
      )

    pageByMethodWithAssertions(
      viewAlternate,
      messageKeyPrefix,
      "agentTrader",
      emptyCDSFullName.EORINo
    )() {
      "the page does not contain the registered business name on the form" in {
        assertNotContainingMessages(
          asDocument(viewAlternate),
          messages("checkRegisteredDetails.heading.2")
        )
      }
    }
  }
}
