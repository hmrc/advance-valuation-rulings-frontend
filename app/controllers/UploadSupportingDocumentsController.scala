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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models._
import models.fileupload._
import models.requests.DataRequest
import pages.UploadSupportingDocumentPage
import repositories.SessionRepository
import services.fileupload.FileUploadService
import views.html.UploadSupportingDocumentsView

@Singleton
class UploadSupportingDocumentsController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  fileUploadService: FileUploadService,
  uploadSupportingDocumentsView: UploadSupportingDocumentsView,
  isThisFileConfidentialController: IsThisFileConfidentialController
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  private val logger = play.api.Logger(this.getClass)

  def onPageLoad(
    error: Option[String],
    key: Option[String],
    uploadId: Option[UploadId],
    mode: Mode,
    draftId: DraftId
  ): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request: DataRequest[AnyContent] =>
      val statusFromCode = error.flatMap(UploadStatus.fromErrorCode)
      uploadId match {
        case None =>
          for {
            result <- fileUploadService.initiateUpload(mode, draftId)
            status  = if (result.uploadStatus == NotStarted) statusFromCode.getOrElse(NotStarted)
                      else result.uploadStatus

          } yield Ok(
            uploadSupportingDocumentsView(
              result.upscanResponse,
              result.redirectFileId,
              status,
              draftId
            )
          )

        case Some(existingFileId) =>
          val fileUploadIds = FileUploadIds.fromExistingUploadId(existingFileId)
          for {
            uploadResult <- fileUploadService.getUploadStatus(existingFileId)
            result       <- uploadResult match {
                              case None                               =>
                                Future(
                                  BadRequest(s"Upload with id $uploadId not found")
                                ) // TODO: NICK: This needs to return the view...
                              case Some(status: UploadedSuccessfully) =>
                                checkForDuplicateUploads(mode, existingFileId, status, draftId)
                              case Some(result)                       =>
                                showUploadForm(fileUploadIds, result, mode, draftId)
                            }
          } yield result
      }
  }

  private def checkForDuplicateUploads(
    mode: Mode,
    existingFileId: UploadId,
    status: UploadedSuccessfully,
    draftId: DraftId
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val uploadedFiles = UploadSupportingDocumentPage.get().toSet
    val fileNames     = uploadedFiles.flatMap(_.files.values.map(_.fileName))
    if (fileNames.contains(status.name)) {
      fileUploadService
        .initiateUpload(mode, draftId)
        .map {
          result =>
            Ok(
              uploadSupportingDocumentsView(
                result.upscanResponse,
                existingFileId,
                DuplicateFile,
                draftId
              )
            )
        }
    } else {
      continueToIsFileConfidential(existingFileId, status, mode, draftId)(request)
    }
  }

  private def continueToIsFileConfidential(
    uploadId: UploadId,
    uploadDetails: UploadedSuccessfully,
    mode: Mode,
    draftId: DraftId
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val payload = UpscanFileDetails(
          uploadId,
          uploadDetails.name,
          uploadDetails.downloadUrl,
          uploadDetails.mimeType,
          uploadDetails.size.getOrElse(0L)
        )

        for {
          answers <-
            UploadSupportingDocumentPage.upsert(
              (uploadedFiles: UploadedFiles) => uploadedFiles.addFile(payload),
              UploadedFiles.initialise(payload)
            )
          _       <- sessionRepository.set(answers)
          _        = logger.info(s"Uploaded file added to session repo uploadId: $uploadId")
        } yield Redirect(
          controllers.routes.IsThisFileConfidentialController.onPageLoad(mode, draftId)
        )
    }

  private def showUploadForm(
    fileUploadIds: FileUploadIds,
    result: UploadStatus,
    mode: Mode,
    draftId: DraftId
  )(implicit
    request: DataRequest[AnyContent]
  ) =
    for {
      response <- fileUploadService.initiateWithExisting(fileUploadIds, mode, draftId)
    } yield Ok(
      uploadSupportingDocumentsView(
        response.upscanResponse,
        response.redirectFileId,
        result,
        draftId
      )
    )
}
