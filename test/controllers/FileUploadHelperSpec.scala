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
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import base.SpecBase
import com.typesafe.config.Config
import controllers.common.FileUploadHelper
import models.{DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import navigation.Navigator
import org.eclipse.jetty.http2.ErrorCode
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.scalacheck.Arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import services.UserAnswersService
import services.fileupload.FileService
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

class FileUploadHelperSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSupportingDocumentsView = mock[UploadSupportingDocumentsView]
  private val mockLetterOfAuthorityView   = mock[UploadLetterOfAuthorityView]
  private val mockMessagesApi             = mock[MessagesApi]
  private val mockFileService             = mock[FileService]
  private val mockNavigator               = mock[Navigator]
  private val mockConfiguration           = mock[Configuration]
  private val mockUserAnswersService      = mock[UserAnswersService]
  private val mockOsClient                = mock[PlayObjectStoreClient]
  private val mockConfig                  = mock[Config]
  private val mockRequestHeader           = FakeRequest()
  private val mockHeaderCarrier           = HeaderCarrier()

  private val mode                   = NormalMode
  private val userAnswers            = userAnswersAsIndividualTrader
  private val expectedViewText       = "html text"
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

  private def getFileUploadHelper: FileUploadHelper = {
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
  }

  private def setMockLetterOfAuthorityView(): Unit = {
    when(
      mockLetterOfAuthorityView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))
  }

  private def setMockSupportingDocumentsView(): Unit = {
    when(
      mockSupportingDocumentsView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))
  }

  private def setMockConfiguration(): Unit = {
    when(mockConfiguration.underlying).thenReturn(mockConfig)
  }

  private def setMockFileService(isLetterOfAuthority: Boolean): Unit = {
    when(
      mockFileService.initiate(
        draftId,
        getUploadControllerPathUrl(isLetterOfAuthority, draftId, mode, None, None),
        isLetterOfAuthority
      )(mockHeaderCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))
  }

  "Check for status" - {

    val initiatedUploadedFile = UploadedFile.Initiated(reference = "a reference")

    "Check for status for letter of authority" in {
      val isLetterOfAuthority = true

      setMockConfiguration()

      val updatedUserAnswers = userAnswers
        .set(UploadLetterOfAuthorityPage, initiatedUploadedFile)
        .success
        .value

      val result = getFileUploadHelper.checkForStatus(updatedUserAnswers, isLetterOfAuthority)
      result.get mustEqual initiatedUploadedFile
    }

    "Check for status for supporting documents" in {
      val isLetterOfAuthority = false

      setMockConfiguration()

      val updatedUserAnswers = userAnswers
        .set(UploadSupportingDocumentPage, initiatedUploadedFile)
        .success
        .value

      val result = getFileUploadHelper.checkForStatus(updatedUserAnswers, isLetterOfAuthority)
      result.get mustEqual initiatedUploadedFile
    }
  }

  "Remove file" in {
    // This boolean value is only required to pass to the method which shows the fallback page.
    val isLetterOfAuthority = true

    //TODO.
    fail
  }

  "Show in progress page" in {
    // This boolean value is only required to pass to UploadInProgressController.
    val isLetterOfAuthority = true

    val key = Some("a key")
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
        val result = FileUploadHelper(
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
      val key = "a key"
      val errorCode = "an error code"

      val result = getFileUploadHelper
        .redirectWithError(
          draftId,
          Some(key),
          errorCode,
          isLetterOfAuthority,
          mode
        )(mockHeaderCarrier)

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
        )(mockRequestHeader, mockHeaderCarrier)

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
