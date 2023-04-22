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

import controllers.actions._
import forms.DeleteDraftFormProvider
import models.{DraftId, NormalMode}
import navigation.Navigator
import pages.DeleteDraftPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeleteDraftView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteDraftController @Inject() (
                                                           override val messagesApi: MessagesApi,
                                                           userAnswersService: UserAnswersService,
                                                           navigator: Navigator,
                                                           identify: IdentifierAction,
                                                           getData: DataRetrievalActionProvider,
                                                           requireData: DataRequiredAction,
                                                           formProvider: DeleteDraftFormProvider,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           view: DeleteDraftView
                                                         )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        Ok(view(form, draftId))
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, draftId))),
            value =>
              if (value) {
                userAnswersService.clear(draftId).map { _ =>
                  Redirect(navigator.nextPage(DeleteDraftPage, NormalMode, request.userAnswers)(request.affinityGroup))
                }
              } else {
                Future.successful(Redirect(navigator.nextPage(DeleteDraftPage, NormalMode, request.userAnswers)(request.affinityGroup)))
              }
          )
    }
}
