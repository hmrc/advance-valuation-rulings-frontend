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

package controllers

import base.SpecBase
import controllers.common.FileUploadHelper
import models.WhatIsYourRoleAsImporter.EmployeeOfOrg
import models.{DraftAttachment, NormalMode, UploadedFile}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import pages._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.AllDocuments
import services.fileupload.FileService
import userrole.{UserRole, UserRoleProvider}

import java.time.Instant
import scala.concurrent.Future

class UploadSupportingDocumentsControllerSpec extends SpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileService)
    reset(mockFileUploadHelper)
  }

  private val isLetterOfAuthority = false

  private val mockFileService      = mock(classOf[FileService])
  private val mockFileUploadHelper = mock(classOf[FileUploadHelper])

  private val successfulFile: UploadedFile.Success = UploadedFile.Success(
    reference = "reference",
    downloadUrl = "downloadUrl",
    uploadDetails = UploadedFile.UploadDetails(
      fileName = "fileName",
      fileMimeType = "fileMimeType",
      uploadTimestamp = Instant.EPOCH,
      checksum = "checksum",
      size = 1337
    )
  )

  "When the page is loaded it must display the expected content" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value
      .set(WhatIsYourRoleAsImporterPage, EmployeeOfOrg)
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FileService].toInstance(mockFileService),
        bind[FileUploadHelper].toInstance(mockFileUploadHelper)
      )
      .build()

    val successTextForHelper = "test upload supporting document"
    when(
      mockFileUploadHelper.onPageLoadWithFileStatus(
        eqTo(NormalMode),
        eqTo(draftId),
        eqTo(None),
        eqTo(None),
        eqTo(Some(successfulFile)),
        eqTo(isLetterOfAuthority)
      )(any(), any())
    )
      .thenReturn(Future.successful(play.api.mvc.Results.Ok(successTextForHelper)))

    val request = FakeRequest(
      GET,
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(NormalMode, draftId, None, None)
        .url
    )

    val result = route(application, request).value
    contentAsString(result) mustEqual successTextForHelper
  }

  "When the maximum number of files is uploaded it must disallow another upload by redirecting" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(
        AllDocuments,
        List(
          DraftAttachment(successfulFile, Some(true)),
          DraftAttachment(successfulFile, Some(true)),
          DraftAttachment(successfulFile, Some(true)),
          DraftAttachment(successfulFile, Some(true)),
          DraftAttachment(successfulFile, Some(true))
        )
      )
      .success
      .value
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

    val mockUserRoleProvider = mock(classOf[UserRoleProvider])
    val mockUserRole         = mock(classOf[UserRole])
    when(mockUserRole.getMaxSupportingDocuments).thenReturn(5)
    when(mockUserRoleProvider.getUserRole(eqTo(userAnswers))).thenReturn(mockUserRole)

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[UserRoleProvider].toInstance(mockUserRoleProvider)
      )
      .build()

    val request = FakeRequest(
      GET,
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(NormalMode, draftId, None, None)
        .url
    )

    val result = route(application, request).value
    status(result) mustBe SEE_OTHER
  }
}
