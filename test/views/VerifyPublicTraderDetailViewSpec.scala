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

import forms.VerifyTraderDetailsFormProvider
import models.{NormalMode, TraderDetailsWithConfirmation}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.VerifyPublicTraderDetailView

class VerifyPublicTraderDetailViewSpec extends ViewBehaviours {

  private val details: TraderDetailsWithConfirmation = TraderDetailsWithConfirmation(traderDetailsWithCountryCode)

  private val doesNotConsentToDisclosureOfPersonalData: TraderDetailsWithConfirmation =
    details.copy(consentToDisclosureOfPersonalData = false)
  private val emptyCDSFullName: TraderDetailsWithConfirmation                         = details.copy(CDSFullName = "")

  private val form: VerifyTraderDetailsFormProvider = app.injector.instanceOf[VerifyTraderDetailsFormProvider]

  private val view: VerifyPublicTraderDetailView = app.injector.instanceOf[VerifyPublicTraderDetailView]

  val viewViaApply: HtmlFormat.Appendable  =
    view.apply(form.apply(Some(details)), NormalMode, draftId, details)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(form.apply(Some(details)), NormalMode, draftId, details, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(form.apply(Some(details)), NormalMode, draftId, details)(fakeRequest, messages)

  "VerifyPublicTraderDetailView" - {
    normalPage("checkRegisteredDetails", Some("agentTrader"))()

    "when the trader does not consent to disclosure of personal data" - {
      val viewAlternate: HtmlFormat.Appendable = view.apply(
        form.apply(Some(doesNotConsentToDisclosureOfPersonalData)),
        NormalMode,
        draftId,
        doesNotConsentToDisclosureOfPersonalData
      )(fakeRequest, messages)

      renderPageWithAssertions(viewAlternate, "checkRegisteredDetails.private", runGenericViewTests = true)(
        doesNotConsentToDisclosureOfPersonalData.EORINo
      ) {
        "display the correct advisory paragraph" in {
          assertContainsMessages(asDocument(viewAlternate), messages("checkRegisteredDetails.private.paragraph.1"))
        }
      }
    }

    "when CDSFullName is an empty string" - {
      val viewAlternate: HtmlFormat.Appendable =
        view.apply(form.apply(Some(emptyCDSFullName)), NormalMode, draftId, emptyCDSFullName)(fakeRequest, messages)

      renderPageWithAssertions(viewAlternate, "checkRegisteredDetails", Some("agentTrader"))(emptyCDSFullName.EORINo) {
        "not contain the registered business name on the form" in {
          assertNotContainingMessages(
            asDocument(viewAlternate),
            messages("checkRegisteredDetails.heading.2")
          )
        }
      }
    }
  }
}
