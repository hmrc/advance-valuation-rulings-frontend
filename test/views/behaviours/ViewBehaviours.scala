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

  protected def normalPage(
    view: HtmlFormat.Appendable,
    messageKeyPrefix: String,
    messageKeySuffix: String,
    messageHeadingArgs: Any*
  )(
    expectedGuidanceKeys: String*
  ): Unit =
    "behave like a normal page" - {
      val suffix = if (messageKeySuffix.isEmpty) "" else s".$messageKeySuffix"

      "rendered" - {
        "have the correct banner title" in {
          val doc         = asDocument(view)
          val bannerTitle = doc.getElementsByClass("hmrc-header__service-name")
          bannerTitle.text mustBe messages(applicationBuilder().build())("service.name")
        }

        "display the correct browser title" in {
          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title$suffix")
        }

        "display the correct page title" in {
          val doc = asDocument(view)
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading$suffix", messageHeadingArgs: _*)
        }

        "display the correct guidance" in {
          val doc = asDocument(view)
          for (key <- expectedGuidanceKeys)
            assertContainsText(doc, messages(applicationBuilder().build())(s"$messageKeyPrefix.$key"))
        }

        "contain a timeout dialog" in {
          val timeoutElm = asDocument(view).select("meta[name=\"hmrc-timeout-dialog\"]")
          if (expectTimeoutDialog) {
            assert(timeoutElm.size == 1)
          } else {
            assert(timeoutElm.size == 0)
          }
        }
      }
    }

  def pageWithExpectedMessages(view: HtmlFormat.Appendable, checks: Seq[(String, String)]): Unit =
    checks.foreach { case (cssSelector, message) =>
      s"element with cssSelector '$cssSelector'" - {

        s"have message '$message'" in {
          val doc  = asDocument(view)
          val elem = doc.select(cssSelector)
          elem.first.text() mustBe message
        }
      }
    }
}
