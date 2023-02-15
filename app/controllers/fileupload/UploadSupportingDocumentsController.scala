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

import cats.syntax.all._
import scala.concurrent.{ExecutionContext, Future}

import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import config.FrontendAppConfig
import connectors.UpscanInitiateConnector
import controllers.IsThisFileConfidentialController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.fileupload._
import models.requests.DataRequest
import pages.UploadSupportingDocumentPage
import repositories.SessionRepository
import services.fileupload.UploadProgressTracker
import views.html.fileupload._

@Singleton
class UploadSupportingDocumentsController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  upscanInitiateConnector: UpscanInitiateConnector,
  uploadProgressTracker: UploadProgressTracker,
  uploadFormView: UploadForm,
  isThisFileConfidentialController: IsThisFileConfidentialController
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val knownS3ErrorCodes = List("entitytoolarge", "entitytoosmall", "rejected", "quarantine")

  def onPageLoad(
    error: Option[String] = None,
    key: Option[String] = None,
    uploadId: Option[UploadId] = None
  ): Action[AnyContent] = (identify andThen getData andThen requireData).async {

    implicit request: DataRequest[AnyContent] =>
      val errorCode = error.flatMap(code => knownS3ErrorCodes.find(_ == code.toLowerCase))
      uploadId match {
        case None                 =>
          val nextUploadFileIds = FileUploadIds.generateNewFileUploadId
          showUploadForm(nextUploadFileIds, errorCode, NotStarted)
        case Some(existingFileId) =>
          val fileUploadIds = FileUploadIds.fromExistingUploadId(existingFileId)
          for {
            uploadResult <- uploadProgressTracker.getUploadResult(existingFileId)
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

  private def storeAnswers(
    request: DataRequest[AnyContent],
    uploadId: UploadId,
    uploadDetails: UploadedSuccessfully
  ) = {
    val payload = UpscanFileDetails(uploadId, uploadDetails.name, uploadDetails.downloadUrl)
    Future
      .fromTry(
        request.userAnswers
          .set(UploadSupportingDocumentPage, payload)
      )
      .flatMap(updatedAnswers => sessionRepository.set(updatedAnswers))
  }

  private def continueToIsFileConfidential(
    uploadId: UploadId,
    uploadDetails: UploadedSuccessfully
  ): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        for {
          uploadResult <- uploadProgressTracker.getUploadResult(uploadId)
          result       <- uploadResult match {
                            case Some(s: UploadedSuccessfully) =>
                              storeAnswers(request, uploadId, s)
                                .flatMap(
                                  _ => isThisFileConfidentialController.onCallback().apply(request)
                                )
                            case _                             => Future.successful(InternalServerError("Something gone wrong"))
                          }
        } yield result
    }

  private def showUploadForm(
    fileUploadIds: FileUploadIds,
    errorCode: Option[String],
    result: UploadStatus
  )(implicit
    request: DataRequest[AnyContent]
  ) = {
    val redirectUrlFileId   = fileUploadIds.redirectUrlFileId
    val requestUploadFileId = fileUploadIds.nextUploadFileId

    val baseUrl     = appConfig.host
    val redirectUrl = controllers.fileupload.routes.UploadSupportingDocumentsController
      .onPageLoad(None, None, Some(redirectUrlFileId))
      .url

    val errorRedirectUrl   = s"$baseUrl/advance-valuation-ruling/v3/hello-world".some
    val successRedirectUrl = s"${baseUrl}$redirectUrl".some

    for {
      response <- upscanInitiateConnector.initiateV2(successRedirectUrl, errorRedirectUrl)
      _        <- uploadProgressTracker.requestUpload(
                    requestUploadFileId,
                    Reference(response.fileReference.reference)
                  )
    } yield Ok(
      uploadFormView(response, errorCode, redirectUrlFileId, result)
    )
  }
}
