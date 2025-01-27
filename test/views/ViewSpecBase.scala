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

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html

import scala.jdk.CollectionConverters._

trait ViewSpecBase extends SpecBase with GuiceOneAppPerSuite {

  override lazy val app: Application = applicationBuilder().build()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  given messages: Messages = app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(fakeRequest)

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String, args: Any*)(
    isError: Boolean = false
  ): Assertion = {
    val errorPrefix = if (isError) {
      messages("error.browser.title.prefix") + " "
    } else {
      ""
    }
    val title       = errorPrefix + messages(expectedMessageKey, args*)
    val serviceName = messages("service.name")
    val suffix      = "GOV.UK"
    val fullTitle   = s"$title - $serviceName - $suffix"
    assertEqualsValue(doc, cssSelector, fullTitle)
  }

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    // <p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertPageHeadingEqualsMessage(doc: Document, expectedMessageKey: String, args: Any*): Any = {
    val headers = doc.getElementsByTag("h1")
    headers.size() match {
      case 0                                    => ()
      case 1 if headers.select("label").isEmpty =>
        headers.first.ownText().replaceAll("\u00a0", " ") mustBe messages(expectedMessageKey, args*)
          .replaceAll("&nbsp;", " ")
      case 1                                    =>
        headers.select("label").text().replaceAll("\u00a0", " ") mustBe messages(expectedMessageKey, args*)
          .replaceAll("&nbsp;", " ")
      case _                                    => throw new RuntimeException(s"Pages should only have (at most) one h1 element. Found ${headers.size}")
    }
  }

  def assertContainsText(doc: Document, text: String): Assertion =
    assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertNotContainsText(doc: Document, text: String): Assertion =
    assert(!doc.toString.contains(text), "\n\ntext " + text + " was rendered on the page.\n")

  def assertContainsMessages(doc: Document, expectedMessageKeys: String*): Unit =
    for (key <- expectedMessageKeys) assertContainsText(doc, messages(key))

  def assertNotContainingMessages(doc: Document, expectedMessageKeys: String*): Unit =
    for (key <- expectedMessageKeys) assertNotContainsText(doc, messages(key))

  def assertNotRenderedByTagWithAttributes(doc: Document, tagName: String, attributes: (String, String)*): Assertion = {
    val elements = doc.getElementsByTag(tagName)

    val notFound = elements.asScala.forall { element =>
      attributes.exists { case (name, value) => element.attr(name) != value }
    }

    assert(notFound, s"\n\nElement $tagName element with the specified attributes was found, but should be missing.\n")
  }

  def assertContainsLabel(
    doc: Document,
    forElement: String,
    expectedText: String,
    expectedHintText: Option[String] = None
  ): Assertion = {
    val labels = doc.getElementsByAttributeValue("for", forElement)
    assert(labels.size == 1, s"\n\nLabel for $forElement was not rendered on the page.")
    val label  = labels.first

    def assertLabel(label: Element, expectedText: String, forElement: String): Assertion =
      assert(label.text() == expectedText, s"\n\nLabel for $forElement was not $expectedText")

    expectedHintText match {
      case Some(hint) =>
        assert(
          doc.getElementsByClass("govuk-hint").first.text == hint,
          s"\n\nLabel for $forElement did not contain hint text $hint"
        )
        assertLabel(label, expectedText, forElement)
      case _          => assertLabel(label, expectedText, forElement)
    }
  }

  def assertElementHasClass(doc: Document, id: String, expectedClass: String): Assertion =
    assert(doc.getElementById(id).hasClass(expectedClass), s"\n\nElement $id does not have class $expectedClass")

}
