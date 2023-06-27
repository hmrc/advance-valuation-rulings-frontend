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
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.RemoveSupportingDocumentFormProvider
import models.{DraftId, Index, Mode, UserAnswers}
import navigation.Navigator
import pages.{RemoveSupportingDocumentPage, UploadSupportingDocumentPage}
import queries.{AllDocuments, DraftAttachmentQuery}
import services.UserAnswersService
import views.html.RemoveSupportingDocumentView

class RemoveSupportingDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: RemoveSupportingDocumentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveSupportingDocumentView,
  osClient: PlayObjectStoreClient
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId, index: Index): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        UploadSupportingDocumentPage(index).get().flatMap(_.fileName) match {
          case Some(fileName) =>
            Ok(view(form, mode, draftId, index, fileName))
          case None           =>
            nextPage(index, mode, request.userAnswers)
        }
    }

  def onSubmit(mode: Mode, draftId: DraftId, index: Index): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val urlAndFileName = for {
          file     <- UploadSupportingDocumentPage(index).get()
          url      <- file.fileUrl
          fileName <- file.fileName
        } yield (url, fileName)

        urlAndFileName match {
          case None                  =>
            Future.successful(nextPage(index, mode, request.userAnswers))
          case Some((url, fileName)) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  Future
                    .successful(BadRequest(view(formWithErrors, mode, draftId, index, fileName))),
                {
                  case false => Future.successful(nextPage(index, mode, request.userAnswers))
                  case true  =>
                    for {
                      ua        <- DraftAttachmentQuery(index).remove()
                      allRemoved = ua.get(AllDocuments).fold(false)(_.isEmpty)
                      ua        <- if (allRemoved) ua.removeFuture(AllDocuments) else Future.successful(ua)
                      _         <- userAnswersService.set(ua)
                      _         <- osClient.deleteObject(Path.File(url))
                    } yield nextPage(index, mode, ua)
                }
              )
        }
    }

  private def nextPage(index: Index, mode: Mode, userAnswers: UserAnswers) =
    Redirect(
      navigator.nextPage(RemoveSupportingDocumentPage(index), mode, userAnswers)
    )
}