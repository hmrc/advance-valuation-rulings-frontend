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

import actions.{DataRequiredAction, DataRetrievalAction, DataRetrievalActionProvider, IdentifierAction}
import forms.IsThisFileConfidentialFormProvider
import models._
import navigation.Navigator
import pages.{IsThisFileConfidentialPage, UploadSupportingDocumentPage}
import services.UserAnswersService
import views.html.IsThisFileConfidentialView

class IsThisFileConfidentialController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: IsThisFileConfidentialFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: IsThisFileConfidentialView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(index: Index, mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.get(IsThisFileConfidentialPage(index)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, index, mode, draftId))
    }

  def onSubmit(index: Index, mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, index, mode, draftId))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(IsThisFileConfidentialPage(index), value))
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(IsThisFileConfidentialPage(index), mode, updatedAnswers)(
                  request.affinityGroup
                )
              )
          )
    }
//        form
//          .bindFromRequest()
//          .fold(
//            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, draftId))),
//            (value: Boolean) =>
//              for {
//                updatedAnswers <- UploadSupportingDocumentPage.modify(
//                                    (files: UploadedFiles) => files.setConfidentiality(value)
//                                  )
//                _              <- userAnswersService.set(updatedAnswers)
//              } yield Redirect(
//                navigator.nextPage(IsThisFileConfidentialPage, mode, updatedAnswers)(
//                  request.affinityGroup
//                )
//              )
//          )
//
//    }
}
