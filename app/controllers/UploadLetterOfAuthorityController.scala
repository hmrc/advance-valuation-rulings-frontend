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

  private val mode: Mode        = NormalMode // TODO: allow other modes other than NormalMode.
  private val maxFileSize: Long = configuration.underlying.getBytes("upscan.maxFileSize") / 1000000L

  def onPageLoad(
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String]
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val answers = request.userAnswers

        answers
          .get(UploadLetterOfAuthorityPage)
          .map {
            case file: UploadedFile.Initiated =>
              errorCode
                .map(errorCode => showErrorPage(draftId, mode, errorForCode(errorCode)))
                .getOrElse {
                  if (key.contains(file.reference)) {
                    showInterstitialPage(draftId)
                  } else {
                    showPage(draftId, mode)
                  }
                }
            case file: UploadedFile.Success   =>
              if (key.contains(file.reference)) {
                continue(mode, answers)
              } else {
                showPage(draftId, mode)
              }
            case file: UploadedFile.Failure   =>
              redirectWithError(
                draftId,
                mode,
                key,
                file.failureDetails.failureReason.toString
              )
          }
          .getOrElse {
            showPage(draftId, mode)
          }

    }

  private def showPage(draftId: DraftId, mode: Mode)(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, mode).map {
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

  private def showErrorPage(draftId: DraftId, mode: Mode, errorMessage: String)(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, mode).map {
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
    draftId: DraftId,
    mode: Mode,
    key: Option[String],
    errorCode: String
  )(implicit request: RequestHeader): Future[Result] =
    fileService.initiate(draftId, mode).map {
      _ =>
        Redirect(
          routes.UploadLetterOfAuthorityController
            .onPageLoad(draftId, Some(errorCode), key)
        )
    }

  private def continue(mode: Mode, answers: UserAnswers): Future[Result] =
    Future.successful(
      Redirect(
        navigator.nextPage(UploadLetterOfAuthorityPage, mode, answers)
      )
    )

  private def errorForCode(code: String)(implicit messages: Messages): String =
    code match {
      // TODO: Verify that these are the correct error messages.
      case "InvalidArgument" =>
        Messages("uploadLetterOfAuthority.error.invalidargument")
      case "EntityTooLarge"  =>
        Messages(s"uploadLetterOfAuthority.error.entitytoolarge", maxFileSize)
      case "EntityTooSmall"  =>
        Messages("uploadLetterOfAuthority.error.entitytoosmall")
      case "Rejected"        =>
        Messages("uploadLetterOfAuthority.error.rejected")
      case "Quarantine"      =>
        Messages("uploadLetterOfAuthority.error.quarantine")
      case "Duplicate"       =>
        Messages("uploadLetterOfAuthority.error.duplicate")
      case _                 =>
        Messages(s"uploadLetterOfAuthority.error.unknown")
    }
}
