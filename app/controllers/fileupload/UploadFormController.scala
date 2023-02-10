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

import play.api.Logging
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import config.FrontendAppConfig
import connectors.{Reference, UpscanInitiateConnector}
import models.fileupload.{UploadedSuccessfully, UploadId}
import models.fileupload.NotStarted
import services.fileupload.UploadProgressTracker
import views.html.fileupload._

@Singleton
class UploadFormController @Inject() (
  upscanInitiateConnector: UpscanInitiateConnector,
  uploadProgressTracker: UploadProgressTracker,
  mcc: MessagesControllerComponents,
  uploadFormView: UploadForm,
  uploadResultView: UploadResult,
  submissionFormView: SubmissionForm,
  errorView: views.html.ErrorTemplate,
  submissionResultView: SubmissionResult
)(implicit appConfig: FrontendAppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with Logging {

  def showV3(
    errorCode: Option[String],
    errorMessage: Option[String],
    errorRequestId: Option[String],
    key: Option[String],
    uploadId: Option[UploadId]
  ): Action[AnyContent] = Action.async {
    implicit request =>
      val nextUploadFileId = UploadId.generate
      uploadId match {
        case None                 =>
          // new file upload / error handling
          val errorRedirectUrl   =
            appConfig.uploadRedirectTargetBase + "/advance-valuation-ruling/v3/hello-world"
          val successRedirectUrl =
            appConfig.uploadRedirectTargetBase + routes.UploadFormController
              .showV3(None, None, None, None, Some(nextUploadFileId))
              .url

          for {
            upscanInitiateResponse <-
              upscanInitiateConnector.initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl))
            _                      <- uploadProgressTracker.requestUpload(
                                        nextUploadFileId,
                                        Reference(upscanInitiateResponse.fileReference.reference)
                                      )
          } yield Ok(
            uploadFormView(upscanInitiateResponse, errorMessage, nextUploadFileId, NotStarted)
          )
        case Some(existingFileId) =>
          // Handle success / existing upload
          uploadProgressTracker
            .getUploadResult(existingFileId)
            .flatMap(
              uploadResult =>
                uploadResult match {
                  case None         => Future(BadRequest(s"Upload with id $uploadId not found"))
                  case Some(result) =>
                    val errorRedirectUrl   =
                      appConfig.uploadRedirectTargetBase + "/advance-valuation-ruling/v3/hello-world"
                    val successRedirectUrl =
                      appConfig.uploadRedirectTargetBase + routes.UploadFormController
                        .showV3(None, None, None, None, Some(existingFileId))
                        .url

                    for {
                      upscanInitiateResponse <-
                        upscanInitiateConnector
                          .initiateV2(Some(successRedirectUrl), Some(errorRedirectUrl))
                      _                      <- uploadProgressTracker.requestUpload(
                                                  nextUploadFileId,
                                                  Reference(upscanInitiateResponse.fileReference.reference)
                                                )
                    } yield Ok(
                      uploadFormView(
                        upscanInitiateResponse,
                        errorMessage,
                        existingFileId,
                        result
                      )
                    )
                }
            )
      }
  }

  private case class SampleForm(field1: String, field2: String, uploadedFileId: UploadId)

  private val sampleForm = Form(
    mapping(
      "field1"         -> text,
      "field2"         -> text,
      "uploadedFileId" -> text.transform[UploadId](UploadId(_), _.value)
    )(SampleForm.apply)(SampleForm.unapply)
  )

  def showSubmissionForm(uploadId: UploadId): Action[AnyContent] = Action.async {
    implicit request =>
      val emptyForm = sampleForm.fill(SampleForm("", "", uploadId))
      for (uploadResult <- uploadProgressTracker.getUploadResult(uploadId))
        yield uploadResult match {
          case Some(s: UploadedSuccessfully) => Ok(submissionFormView(emptyForm, s))
          case _                             => InternalServerError("Something gone wrong")
        }
  }

  def submitFormWithFile(): Action[AnyContent] = Action.async {
    implicit request =>
      sampleForm
        .bindFromRequest()
        .fold(
          errors => Future.successful(BadRequest(s"Problem with a form $errors")),
          _ => {
            logger.info("Form successfully submitted")
            Future.successful(Redirect(routes.UploadFormController.showSubmissionResult))
          }
        )
  }

  def showSubmissionResult(): Action[AnyContent] = Action.async {
    implicit request => Future.successful(Ok(submissionResultView()))
  }
}
