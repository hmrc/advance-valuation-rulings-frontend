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

package controllers.callback

import base.SpecBase
import models.{Done, DraftId, UploadedFile}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.fileupload.FileService

import java.time.Instant
import scala.concurrent.Future

class UploadCallbackControllerSpec extends AnyFreeSpec with SpecBase {

  private val mockFileService: FileService = mock(classOf[FileService])

  private lazy val app: Application = applicationBuilder()
    .overrides(
      bind[FileService].toInstance(mockFileService)
    )
    .build()

  private val requestBody: UploadedFile = UploadedFile.Success(
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

  "callback" - {

    "must call the file service with the correct parameters" in {

      when(mockFileService.update(any(), any(), eqTo(true))(any()))
        .thenReturn(Future.successful(Done))

      val request = FakeRequest(
        routes.UploadCallbackController
          .callback(DraftId(0), isLetterOfAuthority = true)
      )
        .withJsonBody(Json.toJson(requestBody))
      val result  = route(app, request).value

      status(result) mustBe OK

      verify(mockFileService).update(eqTo(DraftId(0)), any(), eqTo(true))(any())
    }
  }
}
