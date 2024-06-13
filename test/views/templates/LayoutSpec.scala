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

package views.templates

import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.html.components.TwoThirdsMainContent
import views.behaviours.ViewBehaviours
import views.html.templates.Layout

class LayoutSpec extends ViewBehaviours {

  private val sampleComponent: TwoThirdsMainContent = new TwoThirdsMainContent
  private val htmlComponent: Html                   = sampleComponent(Html(""))

  private val sampleMessagePrefix = "aboutSimilarGoods"
  private val sampleTitle         = "About the rulings for similar goods - Apply for an Advance Valuation Ruling - GOV.UK"
  private val sampleContent: Html = Html("<h1>About the rulings for similar goods</h1>")

  private val view: Layout = app.injector.instanceOf[Layout]

  val viewViaApply: HtmlFormat.Appendable = view.apply(
    sampleTitle,
    autoCompleteEnabled = false,
    Some(Html("additionalHeadBlock")),
    showBackLink = true,
    showSignOut = true,
    Some(draftId),
    Some(_ => htmlComponent)
  )(sampleContent)(fakeRequest, messages)

  val viewViaRender: HtmlFormat.Appendable = view.render(
    sampleTitle,
    autoCompleteEnabled = false,
    Some(Html("additionalHeadBlock")),
    showBackLink = true,
    showSignOut = true,
    Some(draftId),
    Some(_ => htmlComponent),
    sampleContent,
    fakeRequest,
    messages
  )

  val viewViaF: HtmlFormat.Appendable =
    view.f(sampleTitle, false, Some(Html("additionalHeadBlock")), true, true, Some(draftId), Some(_ => htmlComponent))(
      sampleContent
    )(fakeRequest, messages)

  "Layout" - {
    normalPage(sampleMessagePrefix)()
  }
}
