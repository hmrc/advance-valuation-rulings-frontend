package controllers

import java.time.Instant
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.Configuration
import play.api.http.Status.OK
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContent, MessagesControllerComponents, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import base.SpecBase
import com.typesafe.config.Config
import controllers.common.FileUploadHelper
import models.{DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
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
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import services.UserAnswersService
import services.fileupload.FileService
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}
import views.html.UploadLetterOfAuthorityView

class FileUploadHelperSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockMessagesApi             = mock[MessagesApi]
  private val mockControllerComponents    = mock[MessagesControllerComponents]
  private val mockSupportingDocumentsView = mock[UploadSupportingDocumentsView]
  private val mockLetterOfAuthorityView   = mock[UploadLetterOfAuthorityView]
  private val mockFileService             = mock[FileService]
  private val mockNavigator               = mock[Navigator]
  private val mockConfiguration           = mock[Configuration]
  private val mockUserAnswersService      = mock[UserAnswersService]
  private val mockOsClient                = mock[PlayObjectStoreClient]
  private val mockExecutionContext        = mock[ExecutionContext]
  private val mockRequestHeader           = mock[RequestHeader]
  private val mockHeaderCarrier           = mock[HeaderCarrier]
  private val mockMessages                = mock[Messages]
  private val mockConfig                  = mock[Config]

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

  // Copied from FileUploadHelper
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

  private def buildRequest(): DataRequest[AnyContent] = {
    val userId  = "userId"
    val draftId = DraftId(Arbitrary.arbitrary[Long].sample.get)
    DataRequest(
      FakeRequest(GET, ""),
      userId,
      EoriNumber,
      UserAnswers(userId, draftId, JsObject.empty, Instant.now),
      AffinityGroup.Individual,
      None
    )
  }

  implicit val request = buildRequest()
  implicit val hc      = HeaderCarrierConverter.fromRequest(request)

  "Show fallback page for supporting documents" in {
    val isLetterOfAuthority = false
    val redirectPath        = getRedirectPath(draftId, isLetterOfAuthority, NormalMode)
    val response            =
      mock[Future[UpscanInitiateResponse]] // (upscanInitiateResponse)(mockExecutionContext)

    val expectedView                    = "html text"
    implicit val headerCarrierConverter = mock[HeaderCarrierConverter]

    when(
      mockSupportingDocumentsView
        .apply(draftId, Some(upscanInitiateResponse), Some("errorMessage"))(
          mockMessages,
          mockRequestHeader
        )
    )
      .thenReturn(HtmlFormat.raw(expectedView))

    when(mockConfiguration.underlying).thenReturn(mockConfig)

    when(
      mockFileService.initiate(
        draftId,
        redirectPath,
        isLetterOfAuthority
      )
    )
//      .thenAnswer(response)
      .thenReturn(Future.successful(upscanInitiateResponse))

    val fileUploadHelper = FileUploadHelper(
      mockMessagesApi,
      mockControllerComponents,
      mockSupportingDocumentsView,
      mockLetterOfAuthorityView,
      mockFileService,
      mockNavigator,
      mockConfiguration,
      mockUserAnswersService,
      mockOsClient
    )(mockExecutionContext)

    val result = fileUploadHelper.showFallbackPage(
      NormalMode,
      draftId,
      isLetterOfAuthority
    )(mockRequestHeader)

    contentAsString(result) mustEqual expectedView
  }

  // TODO: Refactor like the previous test.
  "Show fallback page for letter of authority" in {
    val application =
      applicationBuilder(userAnswers = Some(userAnswersAsIndividualTrader)).build()

    running(application) {
      val request = FakeRequest(
        GET,
        routes.UploadLetterOfAuthorityController
          .onPageLoad(NormalMode, draftId, Some("error code"), Some("key"))
          .url
      )

      val result = route(application, request).value

      val view = application.injector.instanceOf[UploadLetterOfAuthorityView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(
        draftId = draftId,
        upscanInitiateResponse = Some(upscanInitiateResponse),
        errorMessage = Some("error code")
      )(
        messages(application),
        request
      ).toString
    }
  }

}
