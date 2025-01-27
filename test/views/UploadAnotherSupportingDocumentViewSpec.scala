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

import forms.UploadAnotherSupportingDocumentFormProvider
import models.{DraftAttachment, NormalMode, UploadedFile}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.UploadAnotherSupportingDocumentView

import java.time.Instant

class UploadAnotherSupportingDocumentViewSpec extends ViewBehaviours {

  private val form: UploadAnotherSupportingDocumentFormProvider =
    app.injector.instanceOf[UploadAnotherSupportingDocumentFormProvider]

  val successfulFile: UploadedFile.Success = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.now(),
      checksum = "checksum",
      size = 1
    )
  )

  private def attachments(numOfDocs: Int): Seq[DraftAttachment] = Seq.fill(numOfDocs)(
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
  )

  private val view: UploadAnotherSupportingDocumentView = app.injector.instanceOf[UploadAnotherSupportingDocumentView]

  val viewViaApply: HtmlFormat.Appendable  =
    view.apply(attachments(2), form.apply(attachments(2)), NormalMode, draftId, None)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(attachments(2), form.apply(attachments(2)), NormalMode, draftId, None, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(attachments(2), form.apply(attachments(2)), NormalMode, draftId, None)(fakeRequest, messages)

  "UploadAnotherSupportingDocumentView" - {
    "when no letter of authority has been uploaded yet" - {
      "must show how many files have been uploaded" - {
        normalPage("uploadAnotherSupportingDocument", Some("many"))(attachments(2).length.toString)
      }
      "must show a single document has been uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view.apply(attachments(1), form.apply(attachments(1)), NormalMode, draftId, None)(fakeRequest, messages)
        renderPage(viewAlternate, "uploadAnotherSupportingDocument", Some("one"))()
      }
      "must show the maximum amount of documents (not including letter of authority) have been uploaded" - {
        val maxAttachments: Int                  = 5
        val viewAlternate: HtmlFormat.Appendable =
          view.apply(attachments(maxAttachments), form.apply(attachments(maxAttachments)), NormalMode, draftId, None)(
            fakeRequest,
            messages
          )
        renderPage(viewAlternate, "uploadAnotherSupportingDocument", Some("max"))()
      }
    }

    "when a letter of authority has been uploaded" - {
      "must show how many files have been uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view.apply(attachments(2), form.apply(attachments(1)), NormalMode, draftId, Some("letterOfAuthority.file"))(
            fakeRequest,
            messages
          )
        renderPage(viewAlternate, "uploadAnotherSupportingDocument.agentForTrader", Some("many"))(
          attachments(2).length.toString
        )
      }
      "must show a single document has been uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view.apply(attachments(1), form.apply(attachments(1)), NormalMode, draftId, Some("letterOfAuthority.file"))(
            fakeRequest,
            messages
          )
        renderPage(viewAlternate, "uploadAnotherSupportingDocument.agentForTrader", Some("one"))()
      }
      "must show the maximum amount of documents (including letter of authority) have been uploaded" - {
        val maxAttachments: Int                  = 4
        val viewAlternate: HtmlFormat.Appendable = view.apply(
          attachments(maxAttachments),
          form.apply(attachments(maxAttachments)),
          NormalMode,
          draftId,
          Some("letterOfAuthority.file")
        )(fakeRequest, messages)
        renderPage(viewAlternate, "uploadAnotherSupportingDocument", Some("max"))()
      }
    }
  }
}
