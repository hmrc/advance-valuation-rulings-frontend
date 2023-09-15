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

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.config.ObjectStoreClientConfig
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import akka.stream.Materializer
import base.SpecBase
import com.typesafe.config.Config
import controllers.common.FileUploadHelper
import models.{Done, DraftAttachment, DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import navigation.Navigator
import org.eclipse.jetty.http2.ErrorCode
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{doReturn, times, verify}
import org.mockito.MockitoSugar.reset
import org.mockito.MockitoSugar.when
import org.scalacheck.Arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import queries.AllDocuments
import services.UserAnswersService
import services.fileupload.FileService
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

class FileUploadHelperSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockSupportingDocumentsView,
      mockLetterOfAuthorityView,
      mockMessagesApi,
      mockFileService,
      mockNavigator,
      mockConfiguration,
      mockUserAnswersService,
      mockOsClient,
      mockConfig
    )
  }

  private val mockSupportingDocumentsView = mock[UploadSupportingDocumentsView]
  private val mockLetterOfAuthorityView   = mock[UploadLetterOfAuthorityView]
  private val mockMessagesApi             = mock[MessagesApi]
  private val mockFileService             = mock[FileService]
  private val mockNavigator               = mock[Navigator]
  private val mockConfiguration           = mock[Configuration]
  private val mockUserAnswersService      = mock[UserAnswersService]
  private val mockOsClient                = mock[PlayObjectStoreClient]
  private val mockConfig                  = mock[Config]

  private val fakeRequestHeader = FakeRequest()
  private val headerCarrier     = HeaderCarrier()

  private val mode                                 = NormalMode
  private val userAnswers                          = userAnswersAsIndividualTrader
  private val expectedViewText                     = "html text"
  private val initiatedUploadedFile                = UploadedFile.Initiated(reference = "a reference")
  private val upscanInitiateResponse               = UpscanInitiateResponse(
    reference = "reference",
    uploadRequest = UpscanInitiateResponse.UploadRequest(
      href = "href",
      fields = Map(
        "field1" -> "value1",
        "field2" -> "value2"
      )
    )
  )
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

  private def getUploadControllerPathUrl(
    isLetterOfAuthority: Boolean,
    draftId: DraftId,
    mode: Mode,
    errorCode: Option[String],
    key: Option[String]
  ): String =
    if (isLetterOfAuthority) {
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(mode, draftId, errorCode, key)
        .url
    } else {
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(mode, draftId, errorCode, key)
        .url
    }

  private def getOnwardPathUrl(
    isLetterOfAuthority: Boolean,
    draftId: DraftId,
    mode: Mode
  ): String =
    if (isLetterOfAuthority) {
      controllers.routes.VerifyLetterOfAuthorityController
        .onPageLoad(mode, draftId)
        .url
    } else {
      controllers.routes.IsThisFileConfidentialController
        .onPageLoad(mode, draftId)
        .url
    }

  private def getFileUploadHelper: FileUploadHelper =
    FileUploadHelper(
      mockMessagesApi,
      mockSupportingDocumentsView,
      mockLetterOfAuthorityView,
      mockFileService,
      mockNavigator,
      mockConfiguration,
      mockUserAnswersService,
      mockOsClient
    )

  private def setUploadedFileInUserAnswers(isLetterOfAuthority: Boolean) =
    if (isLetterOfAuthority) {
      userAnswers.set(UploadLetterOfAuthorityPage, initiatedUploadedFile).success.value
    } else {
      userAnswers.set(UploadSupportingDocumentPage, initiatedUploadedFile).success.value
    }

  private def setMockLetterOfAuthorityView(): Unit =
    when(
      mockLetterOfAuthorityView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))

  private def setMockSupportingDocumentsView(): Unit =
    when(
      mockSupportingDocumentsView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))

  private def setMockConfiguration(): Unit =
    when(mockConfiguration.underlying).thenReturn(mockConfig)

  private def setMockFileService(isLetterOfAuthority: Boolean): Unit =
    when(
      mockFileService.initiate(
        draftId,
        getUploadControllerPathUrl(isLetterOfAuthority, draftId, mode, None, None),
        isLetterOfAuthority
      )(headerCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))

  "Check for status" - {

    def testCheckForStatus(isLetterOfAuthority: Boolean): Unit = {
      setMockConfiguration()

      val updatedUserAnswers = setUploadedFileInUserAnswers(isLetterOfAuthority)

      val result = getFileUploadHelper.checkForStatus(updatedUserAnswers, isLetterOfAuthority)
      result.get mustEqual initiatedUploadedFile
    }

    "Check for status for letter of authority" in {
      testCheckForStatus(isLetterOfAuthority = true)
    }

    "Check for status for supporting documents" in {
      testCheckForStatus(isLetterOfAuthority = false)
    }
  }

  "Remove file" in {
    // This boolean value is only required to pass to the method which shows the fallback page.
    val isLetterOfAuthority = true

    setMockConfiguration()

    when(mockUserAnswersService.set(any())(any()))
      .thenReturn(Future.successful(Done))
    when(mockOsClient.deleteObject(any(), anyString())(any()))
      .thenReturn(Future.successful("thing"))
//    doReturn(Future.successful("thing"))
//      .when(mockOsClient)
//      .deleteObject(any(), anyString())(any())

//    val fileUploadHelper = getFileUploadHelper
//
//    when(fileUploadHelper.showFallbackPage(any(), any(), any())(any(), any()))
//      .thenReturn(Future.successful(upscanInitiateResponse))

//    implicit val m = Materializer
//    val osClient: PlayObjectStoreClient = new PlayObjectStoreClient(m, executioncon)
//    val fileUploadHelper = FileUploadHelper(
//      mockMessagesApi,
//      mockSupportingDocumentsView,
//      mockLetterOfAuthorityView,
//      mockFileService,
//      mockNavigator,
//      mockConfiguration,
//      mockUserAnswersService,
//      osClient
//    )

    val ua =
      userAnswers
        .set(AllDocuments, List(DraftAttachment(successfulFile, Some(false))))
        .success
        .value

    val mockDataRequest = mock[DataRequest[AnyContent]]
    val result          = getFileUploadHelper
      .removeFile(
        mode,
        draftId,
        "file url",
        isLetterOfAuthority
      )(mockDataRequest, headerCarrier)

    verify(mockOsClient, times(1))
      .deleteObject(eqTo(Path.File("downloadUrl")), any())(any())

    status(result) mustEqual OK
    contentAsString(result) mustEqual expectedViewText
  }

  "Show in progress page" in {
    // This boolean value is only required to pass to UploadInProgressController.
    val isLetterOfAuthority = true

    val key         = Some("a key")
    val expectedUrl = controllers.routes.UploadInProgressController
      .onPageLoad(draftId, key, isLetterOfAuthority)
      .url

    setMockConfiguration()
    setMockFileService(isLetterOfAuthority)

    val result = getFileUploadHelper
      .showInProgressPage(
        draftId,
        key,
        isLetterOfAuthority
      )

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual expectedUrl
  }

  "Continue" - {

    def testContinue(isLetterOfAuthority: Boolean): Unit = {
      val application = applicationBuilder().build()

      running(application) {
        val navigator = application.injector.instanceOf[Navigator]
        val result    = FileUploadHelper(
          mockMessagesApi,
          mockSupportingDocumentsView,
          mockLetterOfAuthorityView,
          mockFileService,
          navigator,
          mockConfiguration,
          mockUserAnswersService,
          mockOsClient
        )
          .continue(mode, userAnswers, isLetterOfAuthority)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual getOnwardPathUrl(
          isLetterOfAuthority,
          draftId,
          mode
        )
      }
    }

    "Continue for letter of authority" in {
      val isLetterOfAuthority = true

      setMockConfiguration()

      testContinue(isLetterOfAuthority)
    }

    "Continue for supporting documents" in {
      val isLetterOfAuthority = false

      setMockConfiguration()

      testContinue(isLetterOfAuthority)
    }
  }

  "Redirect with error" - {

    def testRedirectWithError(isLetterOfAuthority: Boolean): Unit = {
      val key       = "a key"
      val errorCode = "an error code"

      val result = getFileUploadHelper
        .redirectWithError(
          draftId,
          Some(key),
          errorCode,
          isLetterOfAuthority,
          mode
        )(headerCarrier)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual getUploadControllerPathUrl(
        isLetterOfAuthority,
        draftId,
        mode,
        Some(errorCode),
        Some(key)
      )
    }

    "Redirect with error for letter of authority" in {
      val isLetterOfAuthority = true

      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)

      testRedirectWithError(isLetterOfAuthority)
    }

    "Redirect with error for supporting documents" in {
      val isLetterOfAuthority = false

      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)

      testRedirectWithError(isLetterOfAuthority)
    }
  }

  "Show fallback page" - {

    def testShowFallbackPage(isLetterOfAuthority: Boolean): Unit = {
      val result = getFileUploadHelper
        .showFallbackPage(
          mode,
          draftId,
          isLetterOfAuthority
        )(fakeRequestHeader, headerCarrier)

      status(result) mustEqual OK
      contentAsString(result) mustEqual expectedViewText
    }

    "Show fallback page for letter of authority" in {
      val isLetterOfAuthority = true

      setMockLetterOfAuthorityView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)

      testShowFallbackPage(isLetterOfAuthority)
    }

    "Show fallback page for supporting documents" in {
      val isLetterOfAuthority = false

      setMockSupportingDocumentsView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)

      testShowFallbackPage(isLetterOfAuthority)
    }
  }

}
