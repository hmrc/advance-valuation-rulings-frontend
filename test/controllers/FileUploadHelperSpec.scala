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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.{Application, Configuration}
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import base.SpecBase
import com.typesafe.config.Config
import controllers.common.FileUploadHelper
import models.{DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.IdiomaticMockito.{returned, DoSomethingOps}
import org.mockito.MockitoSugar.{spy, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import pages.UploadLetterOfAuthorityPage
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

  private val expectedViewText        = "html text"
  private val expectedErrorViewText   = " this is an error"
  private val errorMessage            = "Error from messages file"
  private val errorCode               = "InvalidArgument"
  private val maximumFileSizeMB: Long = 5
  private val page                    = UploadLetterOfAuthorityPage

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
        .onPageLoad(mode, draftId, None, None, false)
        .url
    } else {
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(mode, draftId, None, None)
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

  private def setMockLetterOfAuthorityView(): Unit =
    when(
      mockLetterOfAuthorityView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))

  private def setErrorMockLetterOfAuthorityView(): Unit =
    when(
      mockLetterOfAuthorityView
        .apply(eqTo(draftId), any(), eqTo(Some(errorMessage)))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedErrorViewText))

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
        getRedirectPath(draftId, isLetterOfAuthority, NormalMode),
        isLetterOfAuthority
      )(mockHeaderCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))

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

  "when there is a failed file" - {

    "Show error page when there is an error code passed to onPageLoad" in {

      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setErrorMockLetterOfAuthorityView()

      when(mockConfiguration.underlying).thenReturn(mockConfig)

      when(
        mockFileService.initiate(
          draftId,
          redirectPath,
          isLetterOfAuthority
        )(mockHeaderCarrier)
      )
        .thenReturn(Future.successful(upscanInitiateResponse))

      val fileUploadHelper = spy(
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
      )

      errorMessage willBe returned by fileUploadHelper.errorForCode(eqTo(errorCode))(any[Messages])

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        Some(errorCode),
        None,
        Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority
      )(mock[DataRequest[AnyContent]], mockHeaderCarrier)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual expectedErrorViewText
    }

    def injectView(application: Application) =
      application.injector.instanceOf[UploadLetterOfAuthorityView]

    def mockFileServiceInitiate(): Unit =
      when(
        mockFileService.initiate(eqTo(draftId), eqTo(getRedirectPath()), eqTo(true))(
          any()
        )
      ).thenReturn(Future.successful(upscanInitiateResponse))

    def getRedirectPath(
      errorCode: Option[String] = None,
      key: Option[String] = None
    ): String =
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(NormalMode, draftId, errorCode, key, false)
        .url

    def checkBadRequest(
      errCode: String,
      errMessage: MessagesProvider => String,
      application: Application
    ) = {
      val request =
        FakeRequest(GET, getRedirectPath(errorCode = Some(errCode)))
      val result  = route(application, request).value

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual injectView(application)(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = Some(errMessage(messages(application)))
      )(messages(application), request).toString
    }

    val parameterisedCases = Table(
      ("Error code option string", "Failure Reason", "Failure message"),
      (
        "Quarantine",
        UploadedFile.FailureReason.Quarantine,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.quarantine")(messagesProvider)
      ),
      (
        "Rejected",
        UploadedFile.FailureReason.Rejected,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.rejected")(messagesProvider)
      ),
      (
        "Duplicate",
        UploadedFile.FailureReason.Duplicate,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.duplicate")(messagesProvider)
      ),
      (
        "Unknown",
        UploadedFile.FailureReason.Unknown,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.unknown")(messagesProvider)
      ),
      (
        "InvalidArgument",
        UploadedFile.FailureReason.InvalidArgument,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.invalidargument")(messagesProvider)
      ),
      (
        "EntityTooSmall",
        UploadedFile.FailureReason.EntityTooSmall,
        (messagesProvider: MessagesProvider) =>
          Messages.apply("fileUpload.error.entitytoosmall")(messagesProvider)
      ),
      (
        "EntityTooLarge",
        UploadedFile.FailureReason.EntityTooLarge,
        (messagesProvider: MessagesProvider) =>
          Messages.apply(s"fileUpload.error.entitytoolarge", maximumFileSizeMB)(
            messagesProvider
          )
      )
    )

    "Parameterised: A redirect with an error code renders the error message" in {
      forAll(parameterisedCases) {
        (
          errCode: String,
          _: UploadedFile.FailureReason,
          errMessage: MessagesProvider => String
        ) =>
          val initiatedFile = UploadedFile.Initiated(
            reference = "reference"
          )

          val userAnswers = userAnswersAsIndividualTrader.set(page, initiatedFile).success.value

          val application = applicationBuilder(Some(userAnswers))
            .overrides(bind[FileService].toInstance(mockFileService))
            .build()

          mockFileServiceInitiate()

          checkBadRequest(errCode, errMessage, application)

      }
    }
  }

}
