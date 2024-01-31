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

import forms.AgentForOrgCheckRegisteredDetailsFormProvider
import models.AuthUserType.IndividualTrader
import models.{DraftId, NormalMode, TraderDetailsWithCountryCode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CheckRegisteredDetailsView

class CheckRegisteredDetailsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "checkRegisteredDetails"

  val doesNotConsentToDisclosureOfPersonalData: TraderDetailsWithCountryCode =
    traderDetailsWithCountryCode.copy(consentToDisclosureOfPersonalData = false)

  val form: AgentForOrgCheckRegisteredDetailsFormProvider =
    app.injector.instanceOf[AgentForOrgCheckRegisteredDetailsFormProvider]

  val view: CheckRegisteredDetailsView = app.injector.instanceOf[CheckRegisteredDetailsView]

  val viewViaApply: HtmlFormat.Appendable  =
    view(form.apply(), traderDetailsWithCountryCode, NormalMode, IndividualTrader, DraftId(1L))(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable = view.render(
    form.apply(),
    traderDetailsWithCountryCode,
    NormalMode,
    IndividualTrader,
    DraftId(1L),
    fakeRequest,
    messages
  )
  val viewViaF: HtmlFormat.Appendable      =
    view.f(form.apply(), traderDetailsWithCountryCode, NormalMode, IndividualTrader, DraftId(1L))(fakeRequest, messages)

  "CheckRegisteredDetailsView" - {
    normalPage(messageKeyPrefix, "", traderDetailsWithCountryCode.EORINo)()
  }

  "the trader does not consent to disclosure of personal data" - {
    val viewAlternate =
      view(form.apply(), doesNotConsentToDisclosureOfPersonalData, NormalMode, IndividualTrader, DraftId(1L))(
        fakeRequest,
        messages
      )

    pageByMethodWithAssertions(
      viewAlternate,
      "checkRegisteredDetails.private",
      "",
      doesNotConsentToDisclosureOfPersonalData.EORINo
    )() {
      "contains the correct heading" in {
        assertContainsMessages(
          asDocument(viewAlternate),
          messages("checkRegisteredDetails.private.heading", doesNotConsentToDisclosureOfPersonalData.EORINo)
        )
      }
    }
  }
}
