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

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import models._
import navigation.Navigator
import pages._
import services.fileupload.FileService
import views.html.UploadLetterOfAuthorityView

@Singleton
class UploadLetterOfAuthorityController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  view: UploadLetterOfAuthorityView,
  fileService: FileService,
  navigator: Navigator,
  configuration: Configuration,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val maxFileSize: Long                = configuration.underlying.getBytes("upscan.maxFileSize") / 1000000L
  private val controller                       = controllers.routes.UploadLetterOfAuthorityController
  private val page: QuestionPage[UploadedFile] = UploadLetterOfAuthorityPage

  def onPageLoad(
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String]
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val redirectPath =
          controller
            .onPageLoad(mode, draftId, None, None)
            .url // redirect probably shouldn't have the errorCode in it
        val answers      = request.userAnswers

        answers
          .get(page)
          .map {
            case file: UploadedFile.Initiated =>
              errorCode
                .map(
                  errorCode =>
                    showErrorPage(
                      draftId,
                      errorForCode(errorCode),
                      redirectPath
                    )
                )
                .getOrElse {
                  if (key.contains(file.reference)) {
                    showInterstitialPage(draftId)
                  } else {
                    showPage(draftId, redirectPath)
                  }
                }
            case file: UploadedFile.Success   =>
              if (key.contains(file.reference)) {
                continue(mode, answers)
              } else {
                showPage(draftId, redirectPath)
              }
            case file: UploadedFile.Failure   =>
              redirectWithError(
                mode,
                draftId,
                key,
                file.failureDetails.failureReason.toString,
                redirectPath
              )
          }
          .getOrElse {
            showPage(draftId, redirectPath)
          }
    }

  private def showPage(
    draftId: DraftId,
    redirectPath: String
  )(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, redirectPath, true).map {
      response =>
        Ok(
          view(
            draftId = draftId,
            upscanInitiateResponse = Some(response),
            errorMessage = None
          )
        )
    }

  private def showInterstitialPage(
    draftId: DraftId
  )(implicit request: RequestHeader): Future[Result] =
    Future.successful(
      Ok(
        view(
          draftId = draftId,
          upscanInitiateResponse = None,
          errorMessage = None
        )
      )
    )

  private def showErrorPage(draftId: DraftId, errorMessage: String, redirectPath: String)(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, redirectPath, isLetterOfAuthority = true).map {
      response =>
        BadRequest(
          view(
            draftId = draftId,
            upscanInitiateResponse = Some(response),
            errorMessage = Some(errorMessage)
          )
        )
    }

  private def redirectWithError(
    mode: Mode,
    draftId: DraftId,
    key: Option[String],
    errorCode: String,
    redirectPath: String
  )(implicit request: RequestHeader): Future[Result] =
    fileService.initiate(draftId, redirectPath, isLetterOfAuthority = true).map {
      _ => Redirect(controller.onPageLoad(mode, draftId, Some(errorCode), key))
    }

  private def continue(mode: Mode, answers: UserAnswers): Future[Result] =
    Future.successful(
      Redirect(
        navigator.nextPage(page, mode, answers)
      )
    )

  private def errorForCode(code: String)(implicit messages: Messages): String =
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
