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

  val viewViaApply: HtmlFormat.Appendable
  val viewViaRender: HtmlFormat.Appendable
  val viewViaF: HtmlFormat.Appendable

  protected def normalPage(messageKeyPrefix: String, messageKeySuffix: Option[String] = None)(
    messageHeadingArgs: Any*
  ): Unit =
    "must behave like a normal page" - {
      Seq((".apply", viewViaApply), (".render", viewViaRender), (".f", viewViaF)).foreach { case (method, view) =>
        renderPage(view, messageKeyPrefix, messageKeySuffix, method)(messageHeadingArgs*)
      }

      "and have the correct banner title" in {
        val doc         = asDocument(viewViaApply)
        val bannerTitle = doc.getElementsByClass("govuk-header__service-name")
        bannerTitle.text mustBe messages("service.name")
      }
    }

  protected def renderPage(
    view: HtmlFormat.Appendable,
    messageKeyPrefix: String,
    messageKeySuffix: Option[String] = None,
    method: String = "",
    isError: Boolean = false
  )(messageHeadingArgs: Any*): Unit = {

    val suffix = if (messageKeySuffix.isEmpty) "" else s".${messageKeySuffix.value}"

    val renderMethod = if (method.nonEmpty) {
      s"when rendered - using $method"
    } else {
      "when rendered - using .apply"
    }

    s"$renderMethod" - {

      "display the correct browser title" in {
        val doc = asDocument(view)
        assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title$suffix", messageHeadingArgs*)(isError)
      }

      "display the correct page heading (h1)" in {
        val doc = asDocument(view)
        assertPageHeadingEqualsMessage(doc, s"$messageKeyPrefix.heading$suffix", messageHeadingArgs*)
      }
    }
  }

  protected def renderPageWithAssertions(
    view: HtmlFormat.Appendable,
    messageKeyPrefix: String,
    messageKeySuffix: Option[String] = None,
    method: String = "",
    isError: Boolean = false,
    runGenericViewTests: Boolean = false
  )(messageHeadingArgs: Any*)(assertions: => Unit): Unit = {
    val additionalTests = if (runGenericViewTests) {
      renderPage(view, messageKeyPrefix, messageKeySuffix, method, isError)(messageHeadingArgs*)
      "additionally "
    } else {
      ""
    }

    s"${additionalTests}the rendered page must" - {
      assertions
    }
  }
}
