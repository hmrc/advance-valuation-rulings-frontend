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

import forms.TellUsAboutYourRulingFormProvider
import models.{DraftId, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.TellUsAboutYourRulingView

class TellUsAboutYourRulingViewSpec extends ViewBehaviours with BaseSelectors {

  private val messageKeyPrefix = "tellUsAboutYourRuling"

  private val form: TellUsAboutYourRulingFormProvider =
    app.injector.instanceOf[TellUsAboutYourRulingFormProvider]

  val view: TellUsAboutYourRulingView =
    app.injector.instanceOf[TellUsAboutYourRulingView]

  val viewViaApply: HtmlFormat.Appendable  =
    view(form.apply(), NormalMode, DraftId(1L))(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(form.apply(), NormalMode, DraftId(1L), fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(form.apply(), NormalMode, DraftId(1L))(fakeRequest, messages)

  "TellUsAboutYourRulingView" - {

    behave like normalPage(messageKeyPrefix, "")()

    "should have correct additional content" - {

      val textBoxLabel = "#main-content > div > div > form > div > div > label"

      val expectedContent: Seq[(Object, String)] =
        Seq(
          h2           -> "About the goods",
          p(1)         -> "Examples of information can include",
          bullet(1)    -> "ruling reference numbers - this can be found on ruling decision letters from HMRC",
          bullet(2)    -> "if the ruling has been cancelled or annulled - you will need to tell us why",
          bullet(3)    -> "the date the ruling was granted",
          bullet(4)    -> "the expiry date for the ruling",
          textBoxLabel -> "Can you provide more information about the previous ruling?"
        )

      behave like pageWithExpectedMessages(viewViaApply, expectedContent)
    }
  }
}
