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
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import config.FrontendAppConfig
import controllers.actions._
import controllers.routes.DoYouWantToUploadDocumentsController
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import models.fileupload.UploadId
import models.requests.DataRequest
import navigation.Navigator
import pages.{UploadAnotherSupportingDocumentPage, UploadSupportingDocumentPage}
import repositories.SessionRepository
import views.html.UploadAnotherSupportingDocumentView

class UploadAnotherSupportingDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: UploadAnotherSupportingDocumentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  config: FrontendAppConfig,
  osClient: PlayObjectStoreClient,
  link: views.html.components.Link,
  view: UploadAnotherSupportingDocumentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        makeDocumentRows(request.userAnswers, mode, draftId) match {
          case Some(table) =>
            val preparedForm = UploadAnotherSupportingDocumentPage.get() match {
              case None        => form
              case Some(value) => form.fill(value)
            }

            Future.successful(Ok(view(table, preparedForm, mode, draftId)))
          case None        =>
            redirectToUpdateDocument(mode, draftId)
        }
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        validateFromRequest(form)
          .fold(
            formWithErrors =>
              makeDocumentRows(request.userAnswers, mode, draftId) match {
                case Some(table) =>
                  Future.successful(BadRequest(view(table, formWithErrors, mode, draftId)))
                case None        => redirectToUpdateDocument(mode, draftId)
              },
            value =>
              for {
                answers <- UploadAnotherSupportingDocumentPage.set(value)
                _       <- sessionRepository.set(answers)
              } yield Redirect(
                navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, answers)(
                  request.affinityGroup
                )
              )
          )
    }

  def onDelete(uploadId: String, mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val fileOpt = UploadSupportingDocumentPage.get().flatMap(_.getFile(UploadId(uploadId)))
        fileOpt match {
          case Some(file) =>
            for {
              updatedAnswers <-
                UploadSupportingDocumentPage.modify(
                  _.removeFile(UploadId(uploadId))
                )
              _              <- osClient.deleteObject(
                                  path = Path.File(file.downloadUrl)
                                )
              updatedAnswers <- updatedAnswers.removeFuture(UploadAnotherSupportingDocumentPage)
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, updatedAnswers)(
                request.affinityGroup
              )
            )

          case None =>
            Future.successful(
              Redirect(
                navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, request.userAnswers)(
                  request.affinityGroup
                )
              )
            )
        }
    }

  private def makeDocumentRows(userAnswers: UserAnswers, mode: Mode, draftId: DraftId)(implicit
    messages: Messages
  ) =
    userAnswers
      .get(UploadSupportingDocumentPage) match {
      case Some(uploadedFiles) if uploadedFiles.files.nonEmpty =>
        Some(SupportingDocumentsRows(uploadedFiles, link, mode, draftId))
      case _                                                   => None
    }

  private def redirectToUpdateDocument(mode: Mode, draftId: DraftId) =
    Future.successful(Redirect(DoYouWantToUploadDocumentsController.onPageLoad(mode, draftId)))

  private def validateFromRequest(form: Form[Boolean])(implicit
    request: DataRequest[AnyContent]
  ): Form[Boolean] = {
    val docCount = UploadSupportingDocumentPage.get().map(_.files.size).getOrElse(0)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => formWithErrors,
        value =>
          if (value && docCount >= config.maximumFilesAllowed) {
            form
              .fill(value)
              .withError(
                "value",
                "uploadAnotherSupportingDocument.error.fileCount",
                config.maximumFilesAllowed
              )
          } else {
            form.fill(value)
          }
      )
  }
}
