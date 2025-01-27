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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.UploadLetterOfAuthorityView

class UploadLetterOfAuthorityViewSpec extends ViewBehaviours {

  private val view: UploadLetterOfAuthorityView = app.injector.instanceOf[UploadLetterOfAuthorityView]

  val viewViaApply: HtmlFormat.Appendable  =
    view.apply(draftId, Some(upscanInitiateResponse), None)(messages, fakeRequest)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(draftId, Some(upscanInitiateResponse), None, messages, fakeRequest)
  val viewViaF: HtmlFormat.Appendable      = view.f(draftId, Some(upscanInitiateResponse), None)(messages, fakeRequest)

  "UploadLetterOfAuthorityView" - {
    normalPage("uploadLetterOfAuthority")()

    "when upscan does not return an UpscanInitiateResponse" - {
      val viewAlternate: HtmlFormat.Appendable = view.apply(draftId, None, Some("upscan-error"))(messages, fakeRequest)

      renderPageWithAssertions(viewAlternate, "uploadLetterOfAuthority", isError = true, runGenericViewTests = true)() {
        "have no upscan responses in the upload form related to uploaded files" in {
          assertNotRenderedByTagWithAttributes(
            asDocument(viewAlternate),
            "input",
            "type"  -> "hidden",
            "name"  -> "field1",
            "value" -> "value1"
          )
          assertNotRenderedByTagWithAttributes(
            asDocument(viewAlternate),
            "input",
            "type"  -> "hidden",
            "name"  -> "field2",
            "value" -> "value2"
          )
        }
      }
    }
  }
}
