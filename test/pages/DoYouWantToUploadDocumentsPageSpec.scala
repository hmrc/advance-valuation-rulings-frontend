/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import java.time.Instant

import models.{DraftAttachment, DraftId, Index, UploadedFile, UserAnswers}
import pages.behaviours.PageBehaviours
import queries.{AllDocuments, DraftAttachmentAt}

class DoYouWantToUploadDocumentsPageSpec extends PageBehaviours {

  "DoYouWantToUploadDocumentsPage" - {

    beRetrievable[Boolean](DoYouWantToUploadDocumentsPage)

    beSettable[Boolean](DoYouWantToUploadDocumentsPage)

    beRemovable[Boolean](DoYouWantToUploadDocumentsPage)

    "must remove any uploaded documents when the user chooses no" in {

      val successfulFile  = UploadedFile.Success(
        reference = "reference",
        downloadUrl = "downloadUrl",
        uploadDetails = UploadedFile.UploadDetails(
          fileName = "fileName",
          fileMimeType = "fileMimeType",
          uploadTimestamp = Instant.now(),
          checksum = "checksum",
          size = 1337
        )
      )
      val emptyAnswers    = UserAnswers("userId", DraftId(0))
      val existingAnswers = (for {
        ua <-
          emptyAnswers.set(DraftAttachmentAt(Index(0)), DraftAttachment(successfulFile, Some(true)))
        ua <- ua.set(DraftAttachmentAt(Index(1)), DraftAttachment(successfulFile, Some(false)))
      } yield ua).success.value

      val cleanedUpAnswers =
        existingAnswers.set(DoYouWantToUploadDocumentsPage, false).success.value

      cleanedUpAnswers.get(AllDocuments) mustBe empty
    }
  }
}
