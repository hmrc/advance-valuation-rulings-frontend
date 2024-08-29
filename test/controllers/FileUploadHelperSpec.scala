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

package controllers

import base.SpecBase
import com.typesafe.config.Config
import config.FrontendAppConfig
import controllers.common.FileUploadHelper
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import models.{Done, DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.{Assertion, BeforeAndAfterEach}
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.inject.bind
import play.api.mvc.{AnyContent, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import play.twirl.api.HtmlFormat
import services.UserAnswersService
import services.fileupload.FileService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import userrole.{UserRole, UserRoleProvider}
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileUploadHelperSpec extends SpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSupportingDocumentsView)
    reset(mockLetterOfAuthorityView)
    reset(mockMessagesApi)
    reset(mockFileService)
    reset(mockNavigator)
    reset(mockConfiguration)
    reset(mockUserAnswersService)
    reset(mockOsClient)
    reset(mockConfig)
    reset(mockUserRoleProvider)
    reset(mockUserRole)
  }

  private val mockSupportingDocumentsView = mock(classOf[UploadSupportingDocumentsView])
  private val mockLetterOfAuthorityView   = mock(classOf[UploadLetterOfAuthorityView])
  private val mockMessagesApi             = mock(classOf[MessagesApi])
  private val mockFileService             = mock(classOf[FileService])
  private val mockNavigator               = mock(classOf[Navigator])
  private val mockConfiguration           = mock(classOf[Configuration])
  private val mockUserAnswersService      = mock(classOf[UserAnswersService])
  private val mockOsClient                = mock(classOf[PlayObjectStoreClient])
  private val mockConfig                  = mock(classOf[Config])
  private val frontEndAppConfig           = mock(classOf[FrontendAppConfig])
  private val mockMessages                = mock(classOf[Messages])

  private val mockUserRoleProvider = mock(classOf[UserRoleProvider])
  private val mockUserRole         = mock(classOf[UserRole])

  private val fakeRequestHeader = FakeRequest()
  private val headerCarrier     = HeaderCarrier()

  private val mode                    = NormalMode
  private val userAnswers             = userAnswersAsIndividualTrader
  private val expectedViewText        = "html text"
  private val initiatedUploadedFile   = UploadedFile.Initiated(reference = "a reference")
  private val expectedErrorViewText   = " this is an error"
  private val errorMessage            = "Error from messages file"
  private val errorCode               = "InvalidArgument"
  private val maximumFileSizeMB: Long = 5
  private val page                    = UploadLetterOfAuthorityPage
  private val appName                 = "App name"

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
        .onPageLoad(mode, draftId, errorCode, key, redirectedFromChangeButton = false)
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
      mockUserAnswersService,
      mockOsClient,
      mockUserRoleProvider,
      frontEndAppConfig
    )

  private def setUploadedFileInUserAnswers(isLetterOfAuthority: Boolean) =
    if (isLetterOfAuthority) {
      userAnswers.set(UploadLetterOfAuthorityPage, initiatedUploadedFile).success.value
    } else {
      userAnswers.set(UploadSupportingDocumentPage, initiatedUploadedFile).success.value
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
    ()
  }

  private def setErrorMockLetterOfAuthorityView(): Unit = {
    when(
      mockLetterOfAuthorityView
        .apply(eqTo(draftId), any(), eqTo(Some(errorMessage)))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedErrorViewText))
    ()
  }

  private def setMockSupportingDocumentsView(
  ): Unit = {
    when(
      mockSupportingDocumentsView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None), any())(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedViewText))
    ()
  }

  private def setErrorMockSupportingDocumentsView(): Unit = {
    when(
      mockSupportingDocumentsView
        .apply(eqTo(draftId), any(), eqTo(Some(errorMessage)), any())(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedErrorViewText))
    ()
  }

  private def setMockMessages(): Unit = {
    when(mockMessagesApi.preferred(any[RequestHeader])).thenReturn(mockMessages)
    when(mockMessages("fileUpload.error.invalidargument")).thenReturn(errorMessage)
    ()
  }

  private def setMockConfiguration(): Unit = {
    when(mockConfiguration.underlying).thenReturn(mockConfig)
    ()
  }

  private def setMockFileService(isLetterOfAuthority: Boolean): Unit = {
    when(
      mockFileService.initiate(
        draftId,
        getUploadControllerPathUrl(isLetterOfAuthority, draftId, mode, None, None),
        isLetterOfAuthority
      )(headerCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))
    ()
  }

  private def setMockUserRole(userAnswers: Option[UserAnswers]): Unit = {
    when(mockUserAnswersService.get(any())(any()))
      .thenReturn(Future.successful(userAnswers))

    when(mockUserRoleProvider.getUserRole(any()))
      .thenReturn(mockUserRole)

    when(mockUserRole.getMaxSupportingDocuments).thenReturn(3)
    ()
  }

  "Check for status" - {

    def testCheckForStatus(isLetterOfAuthority: Boolean): Unit = {
      setMockConfiguration()

      val updatedUserAnswers = setUploadedFileInUserAnswers(isLetterOfAuthority)

      val result = getFileUploadHelper.checkForStatus(updatedUserAnswers, isLetterOfAuthority)
      result.get mustBe initiatedUploadedFile
      ()
    }

    "Check for status for letter of authority" in {
      testCheckForStatus(isLetterOfAuthority = true)
    }

    "Check for status for supporting documents" in {
      testCheckForStatus(isLetterOfAuthority = false)
    }
  }

  "Remove file" in {
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

    setMockSupportingDocumentsView()
    setMockUserRole(Some(userAnswers))
    when(frontEndAppConfig.appName).thenReturn(appName)
    when(
      mockOsClient.deleteObject(
        ArgumentMatchers.eq(Path.File(successfulFile.fileUrl.get)),
        ArgumentMatchers.eq(appName)
      )(any[HeaderCarrier])
    )
      .thenReturn(Future.successful(()))
    when(mockUserAnswersService.set(any())(any()))
      .thenReturn(Future.successful(Done))
    when(
      mockFileService.initiate(
        eqTo(draftId),
        eqTo("/advance-valuation-ruling/DRAFT123456789/supporting-documents/upload"),
        eqTo(false)
      )(any())
    )
      .thenReturn(Future.successful(upscanInitiateResponse))

    val application = applicationBuilder(Some(userAnswers))
      .overrides(bind[PlayObjectStoreClient].toInstance(mockOsClient))
      .overrides(bind[FileService].toInstance(mockFileService))
      .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
      .overrides(bind[UploadSupportingDocumentsView].toInstance(mockSupportingDocumentsView))
      .overrides(bind[UserRoleProvider].toInstance(mockUserRoleProvider))
      .build()

    val request = FakeRequest(
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(mode, draftId, Some(errorCode), Some("key"))
    )
      .withFormUrlEncodedBody("value" -> "true")
    val result  = route(application, request).value

    status(result) mustBe OK
    contentAsString(result) mustBe expectedViewText
  }

  "Show in progress page" in {
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

    status(result) mustBe SEE_OTHER
    redirectLocation(result).value mustBe expectedUrl
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
          mockUserAnswersService,
          mockOsClient,
          mockUserRoleProvider,
          frontEndAppConfig
        )
          .continue(mode, userAnswers, isLetterOfAuthority)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe getOnwardPathUrl(
          isLetterOfAuthority,
          draftId,
          mode
        )
        ()
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

    def testRedirectWithError(isLetterOfAuthority: Boolean): Assertion = {
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

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe getUploadControllerPathUrl(
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

    def testShowFallbackPage(
      isLetterOfAuthority: Boolean,
      expectedStatus: Int = OK,
      expectedRedirectUrl: Option[String] = None
    ): Assertion = {
      val result = getFileUploadHelper
        .showFallbackPage(
          mode,
          draftId,
          isLetterOfAuthority
        )(fakeRequestHeader, headerCarrier)

      status(result) mustBe expectedStatus

      expectedRedirectUrl match {
        case Some(url) =>
          redirectLocation(result).value mustBe url
        case None      =>
          contentAsString(result) mustBe expectedViewText
      }
    }

    "must show fallback page for letter of authority" in {
      val isLetterOfAuthority = true

      setMockLetterOfAuthorityView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)

      testShowFallbackPage(isLetterOfAuthority)
    }

    "must show fallback page for supporting documents" in {
      val isLetterOfAuthority = false

      setMockSupportingDocumentsView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)
      setMockUserRole(Some(userAnswers))

      testShowFallbackPage(isLetterOfAuthority)
    }

    "must redirect to /there-is-a-problem when user answers are not found" in {
      val isLetterOfAuthority = false

      setMockSupportingDocumentsView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)
      setMockUserRole(None)

      testShowFallbackPage(
        isLetterOfAuthority = isLetterOfAuthority,
        expectedStatus = SEE_OTHER,
        expectedRedirectUrl = Some(routes.JourneyRecoveryController.onPageLoad().url)
      )
    }
  }
  def spyMockFileUploadHelper(): FileUploadHelper =
    spy(
      FileUploadHelper(
        mockMessagesApi,
        mockSupportingDocumentsView,
        mockLetterOfAuthorityView,
        mockFileService,
        mockNavigator,
        mockUserAnswersService,
        mockOsClient,
        mockUserRoleProvider,
        frontEndAppConfig
      )
    )

  def setUpMockFileService(
    redirectPath: String,
    isLetterOfAuthority: Boolean
  ): OngoingStubbing[Future[UpscanInitiateResponse]] =
    when(
      mockFileService.initiate(
        draftId,
        redirectPath,
        isLetterOfAuthority
      )(headerCarrier)
    )
      .thenReturn(Future.successful(upscanInitiateResponse))

  def getRedirectPath(
    errorCode: Option[String] = None,
    key: Option[String] = None
  ): String =
    controllers.routes.UploadLetterOfAuthorityController
      .onPageLoad(NormalMode, draftId, errorCode, key, redirectedFromChangeButton = false)
      .url

  "when there is an initiated file" - {

    "must show fallback page when there is no key" in {
      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setMockLetterOfAuthorityView()
      when(mockConfiguration.underlying).thenReturn(mockConfig)
      setUpMockFileService(redirectPath, isLetterOfAuthority)
      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        None,
        None,
        Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      verify(fileUploadHelper, times(1)).showFallbackPage(any(), any(), any())(any(), any())
      status(result) mustBe OK
      contentAsString(result) mustBe expectedViewText
    }

    "must show in progress page when there is a key" in {
      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setMockLetterOfAuthorityView()
      when(mockConfiguration.underlying).thenReturn(mockConfig)
      setUpMockFileService(redirectPath, isLetterOfAuthority)
      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        None,
        Some("reference"),
        Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      verify(fileUploadHelper, times(1)).showInProgressPage(any(), any(), any())
      status(result) mustBe SEE_OTHER
    }

    "must return BAD_REQUEST and show UploadLetterOfAuthorityView for isLetterOfAuthority = true when there is an error" in {
      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setErrorMockLetterOfAuthorityView()
      when(mockConfiguration.underlying).thenReturn(mockConfig)
      setUpMockFileService(redirectPath, isLetterOfAuthority)
      setMockMessages()

      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        Some(errorCode),
        None,
        Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe expectedErrorViewText
    }

    "must return BAD_REQUEST and show UploadSupportingDocumentsView for isLetterOfAuthority = false when there is an error" in {
      val isLetterOfAuthority = false

      setErrorMockSupportingDocumentsView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)
      setMockUserRole(Some(userAnswers))
      setMockMessages()

      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        mode = NormalMode,
        draftId = draftId,
        errorCode = Some(errorCode),
        key = None,
        fileStatus = Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority = isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe expectedErrorViewText
    }

    "must redirect to /there-is-a-problem for isLetterOfAuthority = false when there is an error and user answers are not found" in {
      val isLetterOfAuthority = false

      setErrorMockSupportingDocumentsView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)
      setMockUserRole(None)
      setMockMessages()

      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        mode = NormalMode,
        draftId = draftId,
        errorCode = Some(errorCode),
        key = None,
        fileStatus = Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority = isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
    }
  }

  "when there is a Failure file" - {
    "Throw runtime exception when in Failure state" in {
      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setMockLetterOfAuthorityView()
      when(mockConfiguration.underlying).thenReturn(mockConfig)
      setUpMockFileService(redirectPath, isLetterOfAuthority)
      val fileUploadHelper = spyMockFileUploadHelper()

      val result: Future[Result] = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        None,
        Some("reference"),
        Some(
          UploadedFile.Failure(
            reference = "ref",
            failureDetails = UploadedFile.FailureDetails(
              failureReason = UploadedFile.FailureReason.Duplicate,
              failureMessage = None
            )
          )
        ),
        isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      result.failed.futureValue mustBe a[RuntimeException]
    }
  }

  "when there is a successful file" - {

    "must remove the file" in {
      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setMockLetterOfAuthorityView()
      when(mockConfiguration.underlying).thenReturn(mockConfig)
      setUpMockFileService(redirectPath, isLetterOfAuthority)
      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        None,
        None,
        Some(successfulFile),
        isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      verify(mockOsClient, times(1)).deleteObject(eqTo(Path.File(successfulFile.fileUrl.get)), eqTo(appName))(any())

      status(result) mustBe OK
    }
  }

  "when fileStatus is None" - {

    "must show fallback page" in {
      val isLetterOfAuthority = false

      setMockSupportingDocumentsView()
      setMockConfiguration()
      setMockFileService(isLetterOfAuthority)
      setMockUserRole(Some(userAnswers))

      val fileUploadHelper = spyMockFileUploadHelper()

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        mode = NormalMode,
        draftId = draftId,
        errorCode = None,
        key = None,
        fileStatus = None,
        isLetterOfAuthority = isLetterOfAuthority
      )(mock(classOf[DataRequest[AnyContent]]), headerCarrier)

      verify(fileUploadHelper, times(1)).showFallbackPage(any(), any(), any())(any(), any())
      status(result) mustBe OK
      contentAsString(result) mustBe expectedViewText
    }
  }

  "when there is a failed file" - {
    def injectView(application: Application): UploadLetterOfAuthorityView =
      application.injector.instanceOf[UploadLetterOfAuthorityView]

    def mockFileServiceInitiate(): Unit = {
      when(
        mockFileService.initiate(eqTo(draftId), eqTo(getRedirectPath()), eqTo(true))(
          any()
        )
      ).thenReturn(Future.successful(upscanInitiateResponse))
      ()
    }

    def checkBadRequest(
      errCode: String,
      errMessage: MessagesProvider => String,
      application: Application
    ): Assertion = {
      val request =
        FakeRequest(GET, getRedirectPath(errorCode = Some(errCode)))
      val result  = route(application, request).value

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe injectView(application)(
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
        (messagesProvider: MessagesProvider) => Messages.apply("fileUpload.error.quarantine")(messagesProvider)
      ),
      (
        "Rejected",
        UploadedFile.FailureReason.Rejected,
        (messagesProvider: MessagesProvider) => Messages.apply("fileUpload.error.rejected")(messagesProvider)
      ),
      (
        "Duplicate",
        UploadedFile.FailureReason.Duplicate,
        (messagesProvider: MessagesProvider) => Messages.apply("fileUpload.error.duplicate")(messagesProvider)
      ),
      (
        "Unknown",
        UploadedFile.FailureReason.Unknown,
        (messagesProvider: MessagesProvider) => Messages.apply("fileUpload.error.unknown")(messagesProvider)
      ),
      (
        "InvalidArgument",
        UploadedFile.FailureReason.InvalidArgument,
        (messagesProvider: MessagesProvider) => Messages.apply("fileUpload.error.invalidargument")(messagesProvider)
      ),
      (
        "EntityTooSmall",
        UploadedFile.FailureReason.EntityTooSmall,
        (messagesProvider: MessagesProvider) => Messages.apply("fileUpload.error.invalidargument")(messagesProvider)
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
