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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.UploadAnotherSupportingDocumentFormProvider
import models._
import models.requests.DataRequest
import navigation.Navigator
import pages.{UploadAnotherSupportingDocumentPage, UploadLetterOfAuthorityPage}
import queries.AllDocuments
import views.html.UploadAnotherSupportingDocumentView

class UploadAnotherSupportingDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: UploadAnotherSupportingDocumentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: UploadAnotherSupportingDocumentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        val attachments = AllDocuments.get().getOrElse(List.empty)
        val form        = formProvider(attachments)
        val loaFileName = getLetterOfAuthorityFileName(request)
        Ok(view(attachments, form, mode, draftId, loaFileName))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val attachments = AllDocuments.get().getOrElse(List.empty)
        formProvider(attachments)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  view(
                    attachments,
                    formWithErrors,
                    mode,
                    draftId,
                    getLetterOfAuthorityFileName(request)
                  )
                )
              ),
            value =>
              for {
                answers <- UploadAnotherSupportingDocumentPage.set(value)
              } yield Redirect {
                navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, answers)
              }
          )
    }

  private def getLetterOfAuthorityFileName(request: DataRequest[AnyContent]) = {
    val loaFile     = request.userAnswers.get(UploadLetterOfAuthorityPage)
    val loaFileName = loaFile match {
      case Some(file) => file.fileName
      case None       => None
    }
    loaFileName
  }

}
