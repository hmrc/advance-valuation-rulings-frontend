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

import play.api.{Application, Configuration}
import play.api.http.HttpEntity
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.inject.bind
import play.api.mvc.{AnyContent, Cookie, ResponseHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient

import base.SpecBase
import com.typesafe.config.Config
import controllers.common.FileUploadHelper
import models.{Done, DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import models.requests.DataRequest
import models.upscan.UpscanInitiateResponse
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.IdiomaticMockito.{returned, DoSomethingOps}
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.{reset, spy, when}
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import services.UserAnswersService
import services.fileupload.FileService
import userrole.{UserRole, UserRoleProvider}
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
      mockConfig,
      mockUserRoleProvider,
      mockUserRole
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

  private val mockUserRoleProvider = mock[UserRoleProvider]
  private val mockUserRole         = mock[UserRole]

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
        .onPageLoad(mode, draftId, errorCode, key, false)
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
    println("providing mocks")
    FileUploadHelper(
      mockMessagesApi,
      mockSupportingDocumentsView,
      mockLetterOfAuthorityView,
      mockFileService,
      mockNavigator,
      mockConfiguration,
      mockUserAnswersService,
      mockOsClient,
      mockUserRoleProvider
    )
  }

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

  private def setErrorMockLetterOfAuthorityView(): Unit =
    when(
      mockLetterOfAuthorityView
        .apply(eqTo(draftId), any(), eqTo(Some(errorMessage)))(
          any(),
          any()
        )
    )
      .thenReturn(HtmlFormat.raw(expectedErrorViewText))

  private def setMockSupportingDocumentsView(
  ): Unit =
    when(
      mockSupportingDocumentsView
        .apply(eqTo(draftId), eqTo(Some(upscanInitiateResponse)), eqTo(None), any())(
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

  private def setMockUserRole(userAnswers: UserAnswers): Unit = {
    when(mockUserAnswersService.get(any())(any()))
      .thenReturn(Future.successful(Some(userAnswers)))

    when(mockUserRoleProvider.getUserRole(any()))
      .thenReturn(mockUserRole)

    when(mockUserRole.getMaxSupportingDocuments).thenReturn(3)
  }

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
    val userAnswers = userAnswersAsIndividualTrader
      .set(UploadSupportingDocumentPage, successfulFile)
      .success
      .value

    setMockSupportingDocumentsView()
    setMockUserRole(userAnswers)
    when(mockOsClient.deleteObject(any[Path.File], any[String])(any[HeaderCarrier]))
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
          mockOsClient,
          mockUserRoleProvider
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
      setMockUserRole(userAnswers)

      testShowFallbackPage(isLetterOfAuthority)
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
        mockConfiguration,
        mockUserAnswersService,
        mockOsClient,
        mockUserRoleProvider
      )
    )

  def setUpMockFileService(
    redirectPath: String,
    isLetterOfAuthority: Boolean
  ): ScalaOngoingStubbing[Future[UpscanInitiateResponse]] =
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

    "Show fallback page when there is no key" in {
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
      )(mock[DataRequest[AnyContent]], headerCarrier)

      verify(fileUploadHelper, times(1)).showFallbackPage(any(), any(), any())(any(), any())
      status(result) mustEqual OK
      contentAsString(result) mustEqual expectedViewText
    }

    "Show in progress page when there is a key" in {
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
      )(mock[DataRequest[AnyContent]], headerCarrier)

      verify(fileUploadHelper, times(1)).showInProgressPage(any(), any(), any())
      status(result) mustEqual SEE_OTHER
    }

    "Show error page when there is an error code passed in" in {
      val isLetterOfAuthority = true
      val redirectPath        = getRedirectPath()

      setErrorMockLetterOfAuthorityView()
      when(mockConfiguration.underlying).thenReturn(mockConfig)
      setUpMockFileService(redirectPath, isLetterOfAuthority)
      val fileUploadHelper = spyMockFileUploadHelper()

      errorMessage willBe returned by fileUploadHelper.errorForCode(eqTo(errorCode))(any[Messages])

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        Some(errorCode),
        None,
        Some(UploadedFile.Initiated("reference")),
        isLetterOfAuthority
      )(mock[DataRequest[AnyContent]], headerCarrier)

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual expectedErrorViewText
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

      Future.successful(
        Result.apply(
          ResponseHeader.apply(OK, Map(), None),
          HttpEntity.NoEntity,
          None,
          None,
          Seq[Cookie]()
        )
      ) willBe returned by fileUploadHelper.removeFile(
        any(),
        any(),
        any(),
        any()
      )(any(), any())

      val result = fileUploadHelper.onPageLoadWithFileStatus(
        NormalMode,
        draftId,
        None,
        None,
        Some(successfulFile),
        isLetterOfAuthority
      )(mock[DataRequest[AnyContent]], headerCarrier)

      verify(fileUploadHelper, times(1)).removeFile(any(), any(), any(), any())(any(), any())
      status(result) mustEqual OK
    }

  }

  "when there is a failed file" - {
    def injectView(application: Application) =
      application.injector.instanceOf[UploadLetterOfAuthorityView]

    def mockFileServiceInitiate(): Unit =
      when(
        mockFileService.initiate(eqTo(draftId), eqTo(getRedirectPath()), eqTo(true))(
          any()
        )
      ).thenReturn(Future.successful(upscanInitiateResponse))

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
