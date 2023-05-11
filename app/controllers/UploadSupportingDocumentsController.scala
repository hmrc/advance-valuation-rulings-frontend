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

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import models._
import models.requests.DataRequest
import navigation.Navigator
import pages.UploadSupportingDocumentPage
import queries.AllDocuments
import services.fileupload.FileService
import views.html.UploadSupportingDocumentsView

@Singleton
class UploadSupportingDocumentsController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  view: UploadSupportingDocumentsView,
  fileService: FileService,
  navigator: Navigator,
  configuration: Configuration
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val maxFileSize: Long = configuration.underlying.getBytes("upscan.maxFileSize") / 1000000L
  private val maxFiles: Int     = configuration.get[Int]("upscan.maxFiles")

  def onPageLoad(
    index: Index,
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String]
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val answers     = request.userAnswers
        val attachments = answers.get(AllDocuments).getOrElse(Seq.empty)

        if (index.position > attachments.size || index.position >= maxFiles) {
          Future.successful(NotFound)
        } else {

          val file = answers.get(UploadSupportingDocumentPage(index))

          file
            .map {
              case file: UploadedFile.Initiated =>
                errorCode
                  .map(errorCode => showErrorPage(draftId, mode, index, errorForCode(errorCode)))
                  .getOrElse {
                    if (key.contains(file.reference)) {
                      showInterstitialPage(draftId)
                    } else {
                      showPage(draftId, mode, index)
                    }
                  }
              case file: UploadedFile.Success   =>
                if (key.contains(file.reference)) {
                  continue(index, mode, answers)
                } else {
                  showPage(draftId, mode, index)
                }
              case file: UploadedFile.Failure   =>
                redirectWithError(
                  draftId,
                  mode,
                  index,
                  key,
                  file.failureDetails.failureReason.toString
                )
            }
            .getOrElse {
              showPage(draftId, mode, index)
            }
        }
    }

  private def showPage(draftId: DraftId, mode: Mode, index: Index)(implicit
    request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, mode, index).map {
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

  private def showErrorPage(draftId: DraftId, mode: Mode, index: Index, errorMessage: String)(
    implicit request: RequestHeader
  ): Future[Result] =
    fileService.initiate(draftId, mode, index).map {
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
    index: Index,
    key: Option[String],
    errorCode: String
  )(implicit request: RequestHeader): Future[Result] =
    fileService.initiate(draftId, mode, index).map {
      _ =>
        Redirect(
          routes.UploadSupportingDocumentsController
            .onPageLoad(index, mode, draftId, Some(errorCode), key)
        )
    }

  private def continue(index: Index, mode: Mode, answers: UserAnswers)(implicit
    request: DataRequest[_]
  ): Future[Result] =
    Future.successful(
      Redirect(
        navigator.nextPage(UploadSupportingDocumentPage(index), mode, answers)
      )
    )

  private def errorForCode(code: String)(implicit messages: Messages): String =
    code match {
      case "InvalidArgument" =>
        Messages("uploadSupportingDocuments.error.invalidargument")
      case "EntityTooLarge"  =>
        Messages(s"uploadSupportingDocuments.error.entitytoolarge", maxFileSize)
      case "EntityTooSmall"  =>
        Messages("uploadSupportingDocuments.error.entitytoosmall")
      case "Rejected"        =>
        Messages("uploadSupportingDocuments.error.rejected")
      case "Quarantine"      =>
        Messages("uploadSupportingDocuments.error.quarantine")
      case "Duplicate"       =>
        Messages("uploadSupportingDocuments.error.duplicate")
      case _                 =>
        Messages(s"uploadSupportingDocuments.error.unknown")
    }
}
