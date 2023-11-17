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

package controllers

import base.SpecBase
import controllers.common.FileUploadHelper
import models.upscan.UpscanInitiateResponse
import models.{NormalMode, UploadedFile}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks
import pages._
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.fileupload.FileService
import views.html.UploadLetterOfAuthorityView

import java.time.Instant
import scala.concurrent.Future

class UploadLetterOfAuthorityControllerSpec extends SpecBase with BeforeAndAfterEach with TableDrivenPropertyChecks {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFileService, mockFileUploadHelper)
  }

  private val maximumFileSizeMB: Long              = 5
  private val page                                 = UploadLetterOfAuthorityPage
  private def injectView(application: Application) =
    application.injector.instanceOf[UploadLetterOfAuthorityView]

  private val isLetterOfAuthority = true

  private val mockFileService      = mock[FileService]
  private val mockFileUploadHelper = mock[FileUploadHelper]

  private def getRedirectPath(
    errorCode: Option[String] = None,
    key: Option[String] = None
  ): String =
    controllers.routes.UploadLetterOfAuthorityController
      .onPageLoad(NormalMode, draftId, errorCode, key, false)
      .url

  private def mockFileServiceInitiate(): Unit =
    when(
      mockFileService.initiate(eqTo(draftId), eqTo(getRedirectPath()), eqTo(true))(
        any()
      )
    ).thenReturn(Future.successful(upscanInitiateResponse))

  private def verifyFileServiceInitiate(): Unit =
    verify(mockFileService).initiate(
      eqTo(draftId),
      eqTo(getRedirectPath()),
      eqTo(true)
    )(any())

  private def verifyFileServiceInitiateZeroTimes(): Unit =
    verify(mockFileService, times(0))
      .initiate(eqTo(draftId), eqTo(getRedirectPath()), eqTo(true))(any())

  private val upscanInitiateResponse = UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "href",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )

  private val initiatedFile =
    UploadedFile.Initiated("reference")

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

  "When the page is redirected from change button the fallback page is displayed" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadLetterOfAuthorityPage, successfulFile)
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FileService].toInstance(mockFileService),
        bind[FileUploadHelper].toInstance(mockFileUploadHelper)
      )
      .build()

    val successTextForHelper = "test upload letter of authority different message lalalala"
    val okFuture             = Future.successful(play.api.mvc.Results.Ok(successTextForHelper))
    when(
      mockFileUploadHelper
        .showFallbackPage(eqTo(NormalMode), eqTo(draftId), eqTo(isLetterOfAuthority))(any(), any())
    )
      .thenReturn(okFuture)

    val request = FakeRequest(
      GET,
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(NormalMode, draftId, None, None, redirectedFromChangeButton = true)
        .url
    )

    val result = route(application, request).value
    status(result) mustEqual OK
    contentAsString(result) mustEqual successTextForHelper
  }

  "When the file status is success the page executes the continue method" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadLetterOfAuthorityPage, successfulFile)
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FileService].toInstance(mockFileService),
        bind[FileUploadHelper].toInstance(mockFileUploadHelper)
      )
      .build()

    val successTextForHelper = "test upload letter of authority different message"
    val okFuture             = Future.successful(play.api.mvc.Results.Ok(successTextForHelper))
    when(mockFileUploadHelper.continue(NormalMode, userAnswers, isLetterOfAuthority))
      .thenReturn(okFuture)

    val request = FakeRequest(
      GET,
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(NormalMode, draftId, None, None, redirectedFromChangeButton = false)
        .url
    )

    val result = route(application, request).value
    status(result) mustEqual OK
    contentAsString(result) mustEqual successTextForHelper
  }

  "When the file status is not success the page executes the onPageLoadWithFileStatus method" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadLetterOfAuthorityPage, initiatedFile)
      .success
      .value

    val application = applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(
        bind[FileService].toInstance(mockFileService),
        bind[FileUploadHelper].toInstance(mockFileUploadHelper)
      )
      .build()

    val successTextForHelper = "test upload letter of authority"
    val okFuture             = Future.successful(play.api.mvc.Results.Ok(successTextForHelper))
    when(
      mockFileUploadHelper.onPageLoadWithFileStatus(
        eqTo(NormalMode),
        eqTo(draftId),
        eqTo(None),
        eqTo(None),
        eqTo(Some(initiatedFile)),
        eqTo(isLetterOfAuthority)
      )(any(), any())
    )
      .thenReturn(okFuture)

    val request = FakeRequest(
      GET,
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(NormalMode, draftId, None, None, redirectedFromChangeButton = false)
        .url
    )

    val result = route(application, request).value
    status(result) mustEqual OK
    contentAsString(result) mustEqual successTextForHelper
  }

}
