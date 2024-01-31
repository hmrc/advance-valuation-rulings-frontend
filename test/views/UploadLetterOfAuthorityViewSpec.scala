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

import models.DraftId
import models.upscan.UpscanInitiateResponse
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.UploadLetterOfAuthorityView

class UploadLetterOfAuthorityViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "uploadLetterOfAuthority"

  val upscanInitiateResponse: UpscanInitiateResponse = UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "href",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )

  val view: UploadLetterOfAuthorityView = app.injector.instanceOf[UploadLetterOfAuthorityView]

  val viewViaApply: HtmlFormat.Appendable  = view(DraftId(1L), Some(upscanInitiateResponse), None)(messages, fakeRequest)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(DraftId(1L), Some(upscanInitiateResponse), None, messages, fakeRequest)
  val viewViaF: HtmlFormat.Appendable      = view.f(DraftId(1L), Some(upscanInitiateResponse), None)(messages, fakeRequest)

  "UploadLetterOfAuthorityView" - {
    normalPage(messageKeyPrefix, "")()
  }

  "when upscan does not return an UpscanInitiateResponse" - {
    val viewAlternate: HtmlFormat.Appendable = view(DraftId(1L), None, None)(messages, fakeRequest)

    pageByMethodWithAssertions(viewAlternate, messageKeyPrefix, "")() {
      "does not contain upscan responses in upload form" in {
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
