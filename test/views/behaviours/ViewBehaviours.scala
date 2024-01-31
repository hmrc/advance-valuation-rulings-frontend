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

package views.behaviours

import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase {

  val expectTimeoutDialog: Boolean = true

  val viewViaApply: HtmlFormat.Appendable
  val viewViaRender: HtmlFormat.Appendable
  val viewViaF: HtmlFormat.Appendable

  def normalPage(messageKeyPrefix: String, messageKeySuffix: String, messageHeadingArgs: Any*)(
    expectedGuidanceKeys: String*
  ): Unit =
    "behave like a normal page" - {
      Seq(".apply", ".render", ".f").foreach {
        case byMethod @ ".apply"  =>
          pageByMethod(viewViaApply, messageKeyPrefix, messageKeySuffix, messageHeadingArgs: _*)(byMethod)
        case byMethod @ ".render" =>
          pageByMethod(viewViaRender, messageKeyPrefix, messageKeySuffix, messageHeadingArgs: _*)(byMethod)
        case byMethod @ ".f"      =>
          pageByMethod(viewViaF, messageKeyPrefix, messageKeySuffix, messageHeadingArgs: _*)(byMethod)
      }

      "have the correct banner title" in {
        val doc         = asDocument(viewViaApply)
        val bannerTitle = doc.getElementsByClass("hmrc-header__service-name")
        bannerTitle.text mustBe messages(app)("service.name")
      }

      "display the correct guidance" in {
        val doc = asDocument(viewViaApply)
        for (key <- expectedGuidanceKeys)
          assertContainsText(doc, messages(app)(s"$messageKeyPrefix.$key"))
      }

      "contain a timeout dialog" in {
        val timeoutElm = asDocument(viewViaApply).select("meta[name=\"hmrc-timeout-dialog\"]")
        if (expectTimeoutDialog) {
          assert(timeoutElm.size == 1)
        } else {
          assert(timeoutElm.size == 0)
        }
      }
    }

  protected def pageByMethod(
    view: HtmlFormat.Appendable,
    messageKeyPrefix: String,
    messageKeySuffix: String,
    messageHeadingArgs: Any*
  )(byMethod: String = "", isError: Boolean = false): Unit = {

    val suffix = if (messageKeySuffix.isEmpty) "" else s".$messageKeySuffix"

    val renderMethod = if (byMethod.nonEmpty) {
      s"when rendered - using $byMethod"
    } else {
      "when rendered"
    }

    s"$renderMethod" - {

      "display the correct browser title" in {
        val doc = asDocument(view)
        assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title$suffix", messageHeadingArgs: _*)(isError)
      }

      "display the correct page heading (h1)" in {
        val doc = asDocument(view)
        assertPageHeadingEqualsMessage(doc, s"$messageKeyPrefix.heading$suffix", messageHeadingArgs: _*)
      }
    }
  }

  def pageByMethodWithAssertions(
    view: HtmlFormat.Appendable,
    messageKeyPrefix: String,
    messageKeySuffix: String,
    messageHeadingArgs: Any*
  )(byMethod: String = "", isError: Boolean = false)(assertions: => Unit): Unit = {
    pageByMethod(view, messageKeyPrefix: String, messageKeySuffix: String, messageHeadingArgs: _*)(byMethod, isError)
    assertions
  }

}
