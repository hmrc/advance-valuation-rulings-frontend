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

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit val messages: Messages = app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(fakeRequest)

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String, args: Any*)(
    isError: Boolean = false
  ): Assertion = {
    val errorPrefix = if (isError) {
      messages("error.browser.title.prefix") + " "
    } else {
      ""
    }
    val title       = errorPrefix + messages(expectedMessageKey, args: _*)
    val serviceName = messages("service.name")
    val suffix      = "GOV.UK"
    val fullTitle   = s"$title - $serviceName - $suffix"
    assertEqualsValue(doc, cssSelector, fullTitle)
  }

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertPageHeadingEqualsMessage(doc: Document, expectedMessageKey: String, args: Any*): Any = {
    val headers = doc.getElementsByTag("h1")
    headers.size() match {
      case 0                                    => ()
      case 1 if headers.select("label").isEmpty =>
        headers.first
          .ownText()
          .replaceAll("\u00a0", " ") mustBe messages(app)(expectedMessageKey, args: _*)
          .replaceAll("&nbsp;", " ")
      case 1                                    =>
        headers.select("label").text().replaceAll("\u00a0", " ") mustBe messages(app)(
          expectedMessageKey,
          args: _*
        )
          .replaceAll("&nbsp;", " ")
      case _                                    => throw new RuntimeException(s"Pages should only have (at most) one h1 element. Found ${headers.size}")
    }
  }

  def assertContainsText(doc: Document, text: String): Assertion =
    assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertNotContainsText(doc: Document, text: String): Assertion =
    assert(!doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertElementHasText(element: Element, text: String): Assertion =
    assert(element.text.contains(text), "\n\ntext " + text + " was not rendered in the element.\n")

  def assertContainsMessages(doc: Document, expectedMessageKeys: String*): Unit      =
    for (key <- expectedMessageKeys) assertContainsText(doc, messages(app)(key))

  def assertNotContainingMessages(doc: Document, expectedMessageKeys: String*): Unit =
    for (key <- expectedMessageKeys) assertNotContainsText(doc, messages(app)(key))

  def assertRenderedById(doc: Document, id: String): Assertion                       =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")

  def assertRenderedByTagWithAttributes(doc: Document, tagName: String, attributes: (String, String)*): Assertion = {
    val elements = doc.getElementsByTag(tagName)
    assert(elements.size() != 0, s"\n\nElement $tagName was not rendered on the page.\n")

    val found = elements.asScala.exists { element =>
      val allAttributesMatch = attributes.forall { case (name, value) =>
        val attrValue = element.attr(name)
        attrValue == value
      }
      allAttributesMatch
    }

    assert(found, s"\n\nNo $tagName element with the specified attributes was found.\n")
  }

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

    def assertLabel(label: Element, expectedText: String, forElement: String) =
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

  def assertContainsRadioButton(
    doc: Document,
    id: String,
    name: String,
    value: String,
    isChecked: Boolean
  ): Assertion = {
    assertRenderedById(doc, id)
    val radio = doc.getElementById(id)
    assert(radio.attr("name") == name, s"\n\nElement $id does not have name $name")
    assert(radio.attr("value") == value, s"\n\nElement $id does not have value $value")
    if (isChecked) {
      assert(radio.attr("checked") == "checked", s"\n\nElement $id is not checked")
    } else {
      assert(!radio.hasAttr("checked") && radio.attr("checked") != "checked", s"\n\nElement $id is checked")
    }
  }

  def assertLinkContainsHref(doc: Document, id: String, href: String): Assertion = {
    assert(doc.getElementById(id) != null, s"\n\nElement $id is not present")
    assert(doc.getElementById(id).attr("href").contains(href))
  }

  def assertLinkContainsHrefAndText(doc: Document, id: String, href: String, linkText: String): Assertion = {
    assertLinkContainsHref(doc, id, href)
    assertContainsText(doc, linkText)
  }
}
