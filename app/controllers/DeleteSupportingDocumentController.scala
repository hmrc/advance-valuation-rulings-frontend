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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.objectstore.client.Path
import uk.gov.hmrc.objectstore.client.play.PlayObjectStoreClient
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import models.{DraftId, Index, Mode}
import navigation.Navigator
import pages.{UploadAnotherSupportingDocumentPage, UploadSupportingDocumentPage}
import queries.DraftAttachmentQuery
import services.UserAnswersService

@Singleton
class DeleteSupportingDocumentController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  osClient: PlayObjectStoreClient
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onDelete(index: Index, mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        request.userAnswers
          .get(UploadSupportingDocumentPage(index))
          .flatMap(_.fileUrl)
          .map {
            url =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.remove(DraftAttachmentQuery(index)))
                _              <- userAnswersService.set(updatedAnswers)
                _              <- osClient.deleteObject(Path.File(url))
              } yield Redirect(
                navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, updatedAnswers)(
                  request.affinityGroup
                )
              )
          }
          .getOrElse {
            Future.successful(
              Redirect(
                navigator.nextPage(UploadAnotherSupportingDocumentPage, mode, request.userAnswers)(
                  request.affinityGroup
                )
              )
            )
          }
    }
}
