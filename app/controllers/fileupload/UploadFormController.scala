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
import connectors.{Reference, UpscanInitiateConnector}
import controllers.IsThisFileConfidentialController
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.fileupload.{Failed, FileUploadId, NotStarted, UploadedSuccessfully, UploadId, UploadStatus}
import models.requests.DataRequest
import services.fileupload.UploadProgressTracker
import views.html.fileupload._

@Singleton
class UploadFormController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
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

  def showV3(
    errorCode: Option[String],
    errorMessage: Option[String],
    errorRequestId: Option[String],
    key: Option[String],
    uploadId: Option[UploadId]
  ): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request: DataRequest[AnyContent] =>
      uploadId match {
        case None                 =>
          val nextUploadFileId = FileUploadId.generateNewFileUploadId
          showUploadForm(nextUploadFileId, errorMessage, NotStarted)
        case Some(existingFileId) =>
          val uploadFileId = FileUploadId.fromExistingUploadId(existingFileId)
          for {
            uploadResult <- uploadProgressTracker.getUploadResult(existingFileId)
            result       <- uploadResult match {
                              case None                               =>
                                Future(BadRequest(s"Upload with id $uploadId not found"))
                              case Some(status: UploadedSuccessfully) =>
                                continueToIsFileConfidential(existingFileId, status)(request)
                              case Some(result)                       =>
                                showUploadForm(uploadFileId, errorMessage, result)
                            }
          } yield result
      }
  }

  def continueToIsFileConfidential(
    uploadId: UploadId,
    uploadDetails: UploadedSuccessfully
  ): Action[AnyContent] = Action.async {
    implicit request =>
      for {
        uploadResult <- uploadProgressTracker.getUploadResult(uploadId)

        result <- uploadResult match {
                    case Some(s: UploadedSuccessfully) =>
                      isThisFileConfidentialController
                        .onCallback(
                          uploadId,
                          uploadDetails.name,
                          uploadDetails.downloadUrl
                        )
                        .apply(request)
                    case _                             => Future.successful(InternalServerError("Something gone wrong"))
                  }
      } yield result
  }

  // Could be moved out to a service
  private def showUploadForm(
    fileUploadId: FileUploadId,
    errorMessage: Option[String],
    result: UploadStatus
  )(implicit
    request: DataRequest[AnyContent]
  ) = {
    val redirectUrlFileId   = fileUploadId.redirectUrlFileId
    val requestUploadFileId = fileUploadId.nextUploadFileId

    val baseUrl     = appConfig.uploadRedirectTargetBase
    val redirectUrl = routes.UploadFormController
      .showV3(None, None, None, None, Some(redirectUrlFileId))
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
      uploadFormView(response, errorMessage, redirectUrlFileId, result)
    )
  }
}
