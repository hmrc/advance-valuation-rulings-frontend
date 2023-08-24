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
import play.api.mvc.{MessagesControllerComponents, RequestHeader, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import models.{DraftId, Mode, UserAnswers}
import navigation.Navigator
import pages.Page
import services.fileupload.FileService
import views.html.{UploadLetterOfAuthorityView, UploadSupportingDocumentsView}

case class FileUploadHelper @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  supportingDocumentsView: UploadSupportingDocumentsView,
  letterOfAuthorityView: UploadLetterOfAuthorityView,
  fileService: FileService,
  navigator: Navigator,
  configuration: Configuration
)(implicit ec: ExecutionContext)
    extends I18nSupport
    with FrontendBaseController {

  private val maxFileSize: Long = configuration.underlying.getBytes("upscan.maxFileSize") / 1000000L
  def showPage(draftId: DraftId, redirectPath: String, isLetterOfAuthority: Boolean)(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, redirectPath, isLetterOfAuthority).map {
      response =>
        isLetterOfAuthority match {
          case false =>
            Ok(
              supportingDocumentsView(
                draftId = draftId,
                upscanInitiateResponse = Some(response),
                errorMessage = None
              )
            )
          case true  =>
            Ok(
              letterOfAuthorityView(
                draftId = draftId,
                upscanInitiateResponse = Some(response),
                errorMessage = None
              )
            )

        }
    }
  def showInterstitialPage(
    draftId: DraftId
  )(implicit request: RequestHeader): Future[Result] =
    Future.successful(
      Ok(
        supportingDocumentsView(
          draftId = draftId,
          upscanInitiateResponse = None,
          errorMessage = None
        )
      )
    )

  def showErrorPage(
    draftId: DraftId,
    errorMessage: String,
    redirectPath: String,
    isLetterOfAuthority: Boolean
  )(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, redirectPath, isLetterOfAuthority).map {
      response =>
        isLetterOfAuthority match {
          case false =>
            BadRequest(
              supportingDocumentsView(
                draftId = draftId,
                upscanInitiateResponse = Some(response),
                errorMessage = Some(errorMessage)
              )
            )
          case true  =>
            BadRequest(
              letterOfAuthorityView(
                draftId = draftId,
                upscanInitiateResponse = Some(response),
                errorMessage = Some(errorMessage)
              )
            )
        }
    }

  def redirectWithError(
    draftId: DraftId,
    key: Option[String],
    errorCode: String,
    redirectPath: String,
    isLetterOfAuthority: Boolean,
    mode: Mode
  )(implicit request: RequestHeader): Future[Result] =
    fileService.initiate(draftId, redirectPath, isLetterOfAuthority = false).map {
      _ =>
        isLetterOfAuthority match {
          case false =>
            Redirect(
              controllers.routes.UploadSupportingDocumentsController
                .onPageLoad(mode, draftId, Some(errorCode), key)
            )
          case true  =>
            Redirect(
              controllers.routes.UploadLetterOfAuthorityController
                .onPageLoad(draftId, Some(errorCode), key)
            )
        }
    }

  def continue(mode: Mode, answers: UserAnswers, page: Page): Future[Result] =
    Future.successful(
      Redirect(
        navigator.nextPage(page, mode, answers)
      )
    )
  def errorForCode(code: String)(implicit messages: Messages): String        =
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
