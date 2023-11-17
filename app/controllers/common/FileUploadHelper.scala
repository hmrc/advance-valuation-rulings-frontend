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

package controllers.common

import config.FrontendAppConfig
import controllers.routes
import models.UploadedFile.Failure
import models.requests.DataRequest
import models.{DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import navigation.Navigator
import pages.{UploadLetterOfAuthorityPage, UploadSupportingDocumentPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{AnyContent, RequestHeader, Result}
import play.api.{Configuration, Logger}
import services.UserAnswersService
import services.fileupload.FileService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import userrole.UserRoleProvider
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class FileUploadHelper @Inject() (
  override val messagesApi: MessagesApi,
  supportingDocumentsView: UploadSupportingDocumentsView,
  letterOfAuthorityView: UploadLetterOfAuthorityView,
  fileService: FileService,
  navigator: Navigator,
  configuration: Configuration,
  userAnswersService: UserAnswersService,
  osClient: PlayObjectStoreClient,
  userRoleProvider: UserRoleProvider,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  private implicit val logger: Logger = Logger(this.getClass)

  private val maxFileSize: Long = configuration.underlying.getBytes("upscan.maxFileSize") / 1000000L

  def onPageLoadWithFileStatus(
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String],
    fileStatus: Option[UploadedFile],
    isLetterOfAuthority: Boolean
  )(implicit
    request: DataRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] =
    fileStatus
      .map {
        case file: UploadedFile.Initiated =>
          errorCode
            .map(errorCode =>
              showErrorPage(
                draftId,
                errorForCode(errorCode),
                isLetterOfAuthority
              )
            )
            .getOrElse {
              if (key.contains(file.reference)) {
                showInProgressPage(draftId, key, isLetterOfAuthority)
              } else {
                showFallbackPage(mode, draftId, isLetterOfAuthority)
              }
            }
        case file: UploadedFile.Success   =>
          removeFile(mode, draftId, file.fileUrl.get, isLetterOfAuthority)
        case _: Failure                   =>
          logger.error(
            "[FileUploadHelper][onPageLoadWithFileStatus] Unexpected Error: Failure received when uploading file"
          )
          Future.failed(
            new RuntimeException(
              "Unexpected Error: Failure received when uploading file"
            )
          )
      }
      .getOrElse {
        showFallbackPage(mode, draftId, isLetterOfAuthority)
      }

  def checkForStatus(
    userAnswers: UserAnswers,
    isLetterOfAuthority: Boolean
  ): Option[UploadedFile] =
    if (isLetterOfAuthority) {
      userAnswers.get(UploadLetterOfAuthorityPage)
    } else {
      userAnswers.get(UploadSupportingDocumentPage)
    }

  def removeFile(mode: Mode, draftId: DraftId, fileUrl: String, isLetterOfAuthority: Boolean)(implicit
    request: DataRequest[AnyContent],
    hc: HeaderCarrier
  ): Future[Result] = {
    osClient.deleteObject(
      Path.File(fileUrl),
      appConfig.appName
    )
    showFallbackPage(mode, draftId, isLetterOfAuthority)
  }

  def showInProgressPage(
    draftId: DraftId,
    key: Option[String],
    isLetterOfAuthority: Boolean
  ): Future[Result] =
    Future.successful(
      Redirect(
        controllers.routes.UploadInProgressController.onPageLoad(draftId, key, isLetterOfAuthority)
      )
    )

  def continue(mode: Mode, answers: UserAnswers, isLetterOfAuthority: Boolean): Future[Result] = {
    val page = if (isLetterOfAuthority) {
      UploadLetterOfAuthorityPage
    } else {
      UploadSupportingDocumentPage
    }

    Future.successful(
      Redirect(
        navigator.nextPage(page, mode, answers)
      )
    )
  }

  def redirectWithError(
    draftId: DraftId,
    key: Option[String],
    errorCode: String,
    isLetterOfAuthority: Boolean,
    mode: Mode
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val redirectPath = getRedirectPath(draftId, isLetterOfAuthority, mode)

    fileService.initiate(draftId, redirectPath, isLetterOfAuthority).map { _ =>
      if (isLetterOfAuthority) {
        Redirect(
          controllers.routes.UploadLetterOfAuthorityController
            .onPageLoad(mode, draftId, Some(errorCode), key, redirectedFromChangeButton = false)
        )
      } else {
        Redirect(
          controllers.routes.UploadSupportingDocumentsController
            .onPageLoad(mode, draftId, Some(errorCode), key)
        )
      }
    }
  }

  /** If an un unexpected error occurs, the user will be redirected back to the corresponding upload
    * file page.
    */
  def showFallbackPage(mode: Mode, draftId: DraftId, isLetterOfAuthority: Boolean)(implicit
    request: RequestHeader,
    hc: HeaderCarrier
  ): Future[Result] = {
    val redirectPath     = getRedirectPath(draftId, isLetterOfAuthority, mode)
    val eventualResponse = fileService.initiate(draftId, redirectPath, isLetterOfAuthority)
    eventualResponse.flatMap { response =>
      if (isLetterOfAuthority) {
        Future.successful(
          Ok(
            letterOfAuthorityView(
              draftId = draftId,
              upscanInitiateResponse = Some(response),
              errorMessage = None
            )
          )
        )
      } else {
        userAnswersService.get(draftId).map {
          case Some(answers) =>
            val userRole = userRoleProvider.getUserRole(answers)
            Ok(
              supportingDocumentsView(
                draftId = draftId,
                upscanInitiateResponse = Some(response),
                errorMessage = None,
                userRole.getMaxSupportingDocuments
              )
            )
          case None          => Redirect(routes.JourneyRecoveryController.onPageLoad())

        }

      }
    }
  }

  private def showErrorPage(
    draftId: DraftId,
    errorMessage: String,
    isLetterOfAuthority: Boolean
  )(implicit
    request: RequestHeader,
    hc: HeaderCarrier
  ): Future[Result] = {
    val redirectPath = getRedirectPath(draftId, isLetterOfAuthority)

    fileService.initiate(draftId, redirectPath, isLetterOfAuthority).flatMap { response =>
      if (isLetterOfAuthority) {
        Future.successful(
          BadRequest(
            letterOfAuthorityView(
              draftId = draftId,
              upscanInitiateResponse = Some(response),
              errorMessage = Some(errorMessage)
            )
          )
        )
      } else {
        userAnswersService.get(draftId).map {
          case Some(answers) =>
            val userRole = userRoleProvider.getUserRole(answers)

            BadRequest(
              supportingDocumentsView(
                draftId = draftId,
                upscanInitiateResponse = Some(response),
                errorMessage = Some(errorMessage),
                userRole.getMaxSupportingDocuments
              )
            )
          case None          => Redirect(routes.JourneyRecoveryController.onPageLoad())

        }

      }
    }
  }

  private def getRedirectPath(
    draftId: DraftId,
    isLetterOfAuthority: Boolean,
    mode: Mode = NormalMode
  ): String =
    if (isLetterOfAuthority) {
      controllers.routes.UploadLetterOfAuthorityController
        .onPageLoad(mode, draftId, None, None, redirectedFromChangeButton = false)
        .url
    } else {
      controllers.routes.UploadSupportingDocumentsController
        .onPageLoad(mode, draftId, None, None)
        .url
    }

  def errorForCode(code: String)(implicit messages: Messages): String =
    code match {
      case "InvalidArgument" =>
        Messages("fileUpload.error.invalidargument")
      case "EntityTooLarge"  =>
        Messages(s"fileUpload.error.entitytoolarge", maxFileSize)
      case "EntityTooSmall"  =>
        /*
         * As of writing this comment, Upscan has an issue in detecting whether a file has been selected.
         * If the minimum file size is 0, the user will see the error message associated with the "Rejected" case
         * rather than the "InvalidArgument" case they should expect when not selecting a file.
         * The fix is to set a positive minimum file size and display the expected error message via this case.
         * See the application.conf for the minimum file size.
         */
        Messages("fileUpload.error.invalidargument")
      case "Rejected"        =>
        Messages("fileUpload.error.rejected")
      case "Quarantine"      =>
        Messages("fileUpload.error.quarantine")
      case "Duplicate"       =>
        Messages("fileUpload.error.duplicate")
      case _                 =>
        Messages(s"fileUpload.error.unknown")
    }

}
