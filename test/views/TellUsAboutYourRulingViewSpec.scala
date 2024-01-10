/*
 * Copyright 2023 HM Revenue & Customs
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

class TellUsAboutYourRulingViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "tellUsAboutYourRuling"

  private val form: TellUsAboutYourRulingFormProvider =
    app.injector.instanceOf[TellUsAboutYourRulingFormProvider]

  private val view: TellUsAboutYourRulingView =
    app.injector.instanceOf[TellUsAboutYourRulingView]

  private val viewViaApply: HtmlFormat.Appendable  =
    view(form.apply(), NormalMode, DraftId(1L))(fakeRequest, messages)
  private val viewViaRender: HtmlFormat.Appendable =
    view.render(form.apply(), NormalMode, DraftId(1L), fakeRequest, messages)
  private val viewViaF: HtmlFormat.Appendable      =
    view.f(form.apply(), NormalMode, DraftId(1L))(fakeRequest, messages)

  "TellUsAboutYourRulingView" - {

    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" - {
        behave like normalPage(view, messageKeyPrefix, "")()
      }

    val input: Seq[(String, HtmlFormat.Appendable)] =
      Seq(
        ".apply"  -> viewViaApply,
        ".render" -> viewViaRender,
        ".f"      -> viewViaF
      )

    input.foreach { case (method, view) =>
      test(method, view)
    }

    object Selectors extends BaseSelectors {
      val textBoxLabel = "#main-content > div > div > form > div > div > label"
    }

    "view should have correct content" - {

      val expectedContent =
        Seq(
          Selectors.p(1)         -> "Examples of information can include:",
          Selectors.bullet(1)    -> "ruling reference numbers - this can be found on ruling decision letters from HMRC",
          Selectors.bullet(2)    -> "if the ruling has been cancelled or annulled - you will need to tell us why",
          Selectors.bullet(3)    -> "the date the ruling was granted",
          Selectors.bullet(4)    -> "the expiry date for the ruling",
          Selectors.textBoxLabel -> "Can you provide more information about the previous ruling?"
        )

      behave like pageWithExpectedMessages(viewViaApply, expectedContent)
    }
  }
}
