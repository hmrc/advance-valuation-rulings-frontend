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

import cats.syntax.validated
import scala.concurrent.{ExecutionContext, Future}

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import models.fileupload.UploadId
import navigation.Navigator
import pages.{UploadAnotherSupportingDocumentPage, UploadSupportingDocumentPage}
import repositories.SessionRepository
import views.html.UploadAnotherSupportingDocumentView

class UploadAnotherSupportingDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: UploadAnotherSupportingDocumentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  link: views.html.components.Link,
  view: UploadAnotherSupportingDocumentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(UploadSupportingDocumentPage) match {
        case Some(uploadedFiles) =>
          val table        = SupportingDocumentsRows(uploadedFiles, link)
          val count        = table.rows.size
          val preparedForm = request.userAnswers.get(UploadAnotherSupportingDocumentPage) match {
            case None        =>
              form
                .bind(JsObject.apply(Seq("fileCount" -> JsNumber(BigDecimal(count)))))
                .discardingErrors
            case Some(value) =>
              form.fill(UploadAnotherSupportingDocument(value, count))
          }
          Ok(view(count, table, preparedForm, mode))
        case None                =>
          Redirect(
            controllers.fileupload.routes.UploadSupportingDocumentsController
              .onPageLoad(None, None, None)
          )
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              val result = request.userAnswers.get(UploadSupportingDocumentPage) match {
                case Some(uploadedFiles) =>
                  val table = SupportingDocumentsRows(uploadedFiles, link)
                  val count = table.rows.size
                  Ok(view(count, table, formWithErrors, mode))
                case None                =>
                  Redirect(
                    controllers.fileupload.routes.UploadSupportingDocumentsController
                      .onPageLoad(None, None, None)
                  )
              }

              Future.successful(result)
            },
            value => {
              val validated = if (value.value && value.fileCount >= 10) {
                form
                  .fill(value)
                  .withError("value", "uploadAnotherSupportingDocument.error.fileCount")
              } else form

              if (validated.hasErrors) {
                val result = request.userAnswers.get(UploadSupportingDocumentPage) match {
                  case Some(uploadedFiles) =>
                    val table = SupportingDocumentsRows(uploadedFiles, link)
                    val count = table.rows.size
                    Ok(view(count, table, validated, mode))
                  case None                =>
                    Redirect(
                      controllers.fileupload.routes.UploadSupportingDocumentsController
                        .onPageLoad(None, None, None)
                    )
                }

                Future.successful(result)
              } else {
                for {
                  answers <-
                    request.userAnswers.setFuture(UploadAnotherSupportingDocumentPage, value.value)
                  _       <- sessionRepository.set(answers)
                } yield Redirect(
                  navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, answers)
                )
              }
            }
          )
    }

  def onDelete(uploadId: String) = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        updatedAnswers <-
          request.userAnswers.modifyFuture(
            UploadSupportingDocumentPage,
            (uploadedFiles: UploadedFiles) => uploadedFiles.removeFile(UploadId(uploadId))
          )
        updatedAnswers <- updatedAnswers.removeFuture(UploadAnotherSupportingDocumentPage)
        _              <- sessionRepository.set(updatedAnswers)
        r               =
          Redirect(
            navigator.nextPage(UploadAnotherSupportingDocumentPage, NormalMode, updatedAnswers)
          )
      } yield r
  }
}
