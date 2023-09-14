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
import models.{DraftId, Mode, NormalMode, UserAnswers}
import models.NormalMode
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.scalacheck.Arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
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

  private def getRedirectPath(
    draftId: DraftId,
    isLetterOfAuthority: Boolean,
    mode: Mode = NormalMode
  ) =
    if (isLetterOfAuthority) {
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(mode, draftId, None, None)
        .url
    } else {
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(mode, draftId, None, None)
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
        getRedirectPath(draftId, isLetterOfAuthority, NormalMode),
        isLetterOfAuthority
      )(mockHeaderCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))
  }

  "Show fallback page for letter of authority" in {
    val isLetterOfAuthority = true

    setMockLetterOfAuthorityView()
    setMockConfiguration()
    setMockFileService(isLetterOfAuthority)

    val result = getFileUploadHelper
      .showFallbackPage(
        NormalMode,
        draftId,
        isLetterOfAuthority
      )(mockRequestHeader, mockHeaderCarrier)

    contentAsString(result) mustEqual expectedViewText
  }

  "Show fallback page for supporting documents" in {
    val isLetterOfAuthority = false
    val redirectPath        = getRedirectPath(draftId, isLetterOfAuthority, NormalMode)
    val expectedViewText    = "html text"

    when(
      mockSupportingDocumentsView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))

    when(mockConfiguration.underlying).thenReturn(mockConfig)

    when(
      mockFileService.initiate(
        draftId,
        redirectPath,
        isLetterOfAuthority
      )(mockHeaderCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))

    val fileUploadHelper = FileUploadHelper(
      mockMessagesApi,
      mockSupportingDocumentsView,
      mockLetterOfAuthorityView,
      mockFileService,
      mockNavigator,
      mockConfiguration,
      mockUserAnswersService,
      mockOsClient
    )

    val result = fileUploadHelper.showFallbackPage(
      NormalMode,
      draftId,
      isLetterOfAuthority
    )(mockRequestHeader, mockHeaderCarrier)

    contentAsString(result) mustEqual expectedViewText
  }

}
