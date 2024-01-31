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

import forms.UploadAnotherSupportingDocumentFormProvider
import models.{DraftAttachment, DraftId, NormalMode, UploadedFile}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.UploadAnotherSupportingDocumentView

import java.time.Instant

class UploadAnotherSupportingDocumentViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "uploadAnotherSupportingDocument"

  val form: UploadAnotherSupportingDocumentFormProvider =
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

  val singleAttachment: Seq[DraftAttachment] = Seq(
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
  )

  val attachments: Seq[DraftAttachment] = Seq(
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
  )

  val maxAttachments: Seq[DraftAttachment] = Seq(
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
    DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
  )

  val view: UploadAnotherSupportingDocumentView = app.injector.instanceOf[UploadAnotherSupportingDocumentView]

  val viewViaApply: HtmlFormat.Appendable  =
    view(attachments, form.apply(attachments), NormalMode, DraftId(1L), None)(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(attachments, form.apply(attachments), NormalMode, DraftId(1L), None, fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(attachments, form.apply(attachments), NormalMode, DraftId(1L), None)(fakeRequest, messages)

  "UploadAnotherSupportingDocumentView" - {
    "when no letter of authority has been uploaded yet" - {
      "information shows how many uploaded files have been uploaded" - {
        normalPage(messageKeyPrefix, "many", attachments.length.toString)()
      }
      "and there has been a single document uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view(singleAttachment, form.apply(singleAttachment), NormalMode, DraftId(1L), None)(fakeRequest, messages)
        pageByMethod(viewAlternate, messageKeyPrefix, "one")()
      }
      "and the maximum documents (not including letter of authority) have been uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view(maxAttachments, form.apply(maxAttachments), NormalMode, DraftId(1L), None)(fakeRequest, messages)
        pageByMethod(viewAlternate, messageKeyPrefix, "max")()
      }

    }
    "when a letter of authority has been uploaded" - {
      "information shows how many uploaded files have been uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view(attachments, form.apply(singleAttachment), NormalMode, DraftId(1L), Some("letterOfAuthority.file"))(
            fakeRequest,
            messages
          )
        pageByMethod(viewAlternate, s"$messageKeyPrefix.agentForTrader", "many", attachments.length.toString)()
      }
      "and there has been a single document uploaded" - {
        val viewAlternate: HtmlFormat.Appendable =
          view(singleAttachment, form.apply(singleAttachment), NormalMode, DraftId(1L), Some("letterOfAuthority.file"))(
            fakeRequest,
            messages
          )
        pageByMethod(viewAlternate, s"$messageKeyPrefix.agentForTrader", "one")()
      }
      "and the maximum documents (including letter of authority) have been uploaded" - {
        val maxAttachments: Seq[DraftAttachment] = Seq(
          DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
          DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
          DraftAttachment(successfulFile, isThisFileConfidential = Some(false)),
          DraftAttachment(successfulFile, isThisFileConfidential = Some(false))
        )
        val viewAlternate: HtmlFormat.Appendable =
          view(maxAttachments, form.apply(maxAttachments), NormalMode, DraftId(1L), Some("letterOfAuthority.file"))(
            fakeRequest,
            messages
          )
        pageByMethod(viewAlternate, messageKeyPrefix, "max")()
      }
    }
  }
}
