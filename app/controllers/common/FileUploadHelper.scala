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

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents, RequestHeader, Result}
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import models.{DraftId, Mode, NormalMode, UploadedFile, UserAnswers}
import models.requests.DataRequest
import navigation.Navigator
import pages.{Page, QuestionPage}
import services.UserAnswersService
import services.fileupload.FileService
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

case class FileUploadHelper @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  supportingDocumentsView: UploadSupportingDocumentsView,
  letterOfAuthorityView: UploadLetterOfAuthorityView,
  fileService: FileService,
  navigator: Navigator,
  configuration: Configuration,
  userAnswersService: UserAnswersService,
  osClient: PlayObjectStoreClient
)(implicit ec: ExecutionContext)
    extends I18nSupport
    with FrontendBaseController {

  private val maxFileSize: Long = configuration.underlying.getBytes("upscan.maxFileSize") / 1000000L

  /** If an un unexpected error occurs, the user will be redirected back to the corresponding upload
    * file page.
    */
  def showFallbackPage(mode: Mode, draftId: DraftId, isLetterOfAuthority: Boolean)(implicit
    request: RequestHeader
  ): Future[Result] = {
    val redirectPath = getRedirectPath(draftId, isLetterOfAuthority, mode)
    fileService.initiate(draftId, redirectPath, isLetterOfAuthority).map {
      response =>
        Ok(
          supportingDocumentsView(
            draftId = draftId,
            upscanInitiateResponse = Some(response),
            errorMessage = None
          )
        )
    }
  }

  def checkForStatus(
    userAnswers: UserAnswers,
    page: QuestionPage[UploadedFile]
  ): Option[UploadedFile] =
    userAnswers.get(page)

  def removeFile(mode: Mode, draftId: DraftId, fileUrl: String)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {
    osClient.deleteObject(Path.File(fileUrl))
    showFallbackPage(mode, draftId, isLetterOfAuthority = false)
  }

  def showInProgressPage(
    draftId: DraftId,
    key: Option[String]
  ): Future[Result] =
    Future.successful(
      Redirect(controllers.routes.UploadInProgressController.onPageLoad(draftId, key))
    )

  def showErrorPage(
    draftId: DraftId,
    errorMessage: String,
    isLetterOfAuthority: Boolean
  )(implicit
    request: RequestHeader
  ): Future[Result] = {
    val redirectPath = getRedirectPath(draftId, isLetterOfAuthority)

    fileService.initiate(draftId, redirectPath, isLetterOfAuthority).map {
      response =>
        if (isLetterOfAuthority) {
          BadRequest(
            letterOfAuthorityView(
              draftId = draftId,
              upscanInitiateResponse = Some(response),
              errorMessage = Some(errorMessage)
            )
          )
        } else {
          BadRequest(
            supportingDocumentsView(
              draftId = draftId,
              upscanInitiateResponse = Some(response),
              errorMessage = Some(errorMessage)
            )
          )
        }
    }
  }

  def redirectWithError(
    draftId: DraftId,
    key: Option[String],
    errorCode: String,
    isLetterOfAuthority: Boolean,
    mode: Mode
  )(implicit request: RequestHeader): Future[Result] = {
    val redirectPath = getRedirectPath(draftId, isLetterOfAuthority, mode)

    fileService.initiate(draftId, redirectPath, isLetterOfAuthority = false).map {
      _ =>
        if (isLetterOfAuthority) {
          Redirect(
            controllers.routes.UploadLetterOfAuthorityController
              .onPageLoad(mode, draftId, Some(errorCode), key)
          )
        } else {
          Redirect(
            controllers.routes.UploadSupportingDocumentsController
              .onPageLoad(mode, draftId, Some(errorCode), key)
          )
        }
    }
  }

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

  def continue(mode: Mode, answers: UserAnswers, page: Page): Future[Result] =
    Future.successful(
      Redirect(
        navigator.nextPage(page, mode, answers)
      )
    )

  def errorForCode(code: String)(implicit messages: Messages): String =
    code match {
      case "InvalidArgument" =>
        Messages("fileUpload.error.invalidargument")
      case "EntityTooLarge"  =>
        Messages(s"fileUpload.error.entitytoolarge", maxFileSize)
      case "EntityTooSmall"  =>
        Messages("fileUpload.error.entitytoosmall")
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
