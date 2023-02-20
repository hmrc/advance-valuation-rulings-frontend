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

package controllers.fileupload

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import config.FrontendAppConfig
import controllers.IsThisFileConfidentialController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
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

  private val knownS3ErrorCodes = List("entitytoolarge", "entitytoosmall", "rejected", "quarantine")

  def onPageLoad(
    error: Option[String],
    key: Option[String],
    uploadId: Option[UploadId]
  ): Action[AnyContent] = (identify andThen getData andThen requireData).async {

    implicit request: DataRequest[AnyContent] =>
      val errorCode =
        error.map(code => knownS3ErrorCodes.find(_ == code.toLowerCase).getOrElse("unknown"))
      uploadId match {
        case None =>
          for {
            result <- fileUploadService.initiateUpload()
          } yield Ok(
            uploadSupportingDocumentsView(
              result.upscanResponse,
              errorCode,
              result.redirectFileId,
              result.uploadStatus
            )
          )

        case Some(existingFileId) =>
          val fileUploadIds = FileUploadIds.fromExistingUploadId(existingFileId)
          for {
            uploadResult <- fileUploadService.getUploadStatus(existingFileId)
            result       <- uploadResult match {
                              case None                               =>
                                Future(BadRequest(s"Upload with id $uploadId not found"))
                              case Some(status: UploadedSuccessfully) =>
                                continueToIsFileConfidential(existingFileId, status)(request)
                              case Some(result)                       =>
                                showUploadForm(fileUploadIds, errorCode, result)
                            }
          } yield result
      }
  }

  private def continueToIsFileConfidential(
    uploadId: UploadId,
    uploadDetails: UploadedSuccessfully
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val payload = UpscanFileDetails(uploadId, uploadDetails.name, uploadDetails.downloadUrl)

        for {
          answers <- request.userAnswers.setFuture(UploadSupportingDocumentPage, payload)
          _       <- sessionRepository.set(answers)
          r       <- isThisFileConfidentialController.onCallback().apply(request)
        } yield r
    }

  private def showUploadForm(
    fileUploadIds: FileUploadIds,
    errorCode: Option[String],
    result: UploadStatus
  )(implicit
    request: DataRequest[AnyContent]
  ) =
    for {
      response <- fileUploadService.initiateWithExisting(fileUploadIds)
    } yield Ok(
      uploadSupportingDocumentsView(
        response.upscanResponse,
        errorCode,
        response.redirectFileId,
        result
      )
    )
}
