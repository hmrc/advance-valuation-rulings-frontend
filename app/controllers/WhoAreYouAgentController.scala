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
import forms.WhoAreYouAgentFormProvider
import models.{DraftId, Mode}
import navigation.Navigator
import pages.WhoAreYouAgentPage
import services.UserAnswersService
import views.html.WhoAreYouAgentView

class WhoAreYouAgentController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  actions: Actions,
  formProvider: WhoAreYouAgentFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhoAreYouAgentView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (actions.identifyDraft(draftId)) {
      implicit request =>
        val _ = request.userAnswers.get(WhoAreYouAgentPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        // TODO: implement agent details page after MVP
        Redirect(controllers.routes.AccountHomeController.onPageLoad())
      // Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (actions.identifyWithHistory(draftId, WhoAreYouAgentPage)).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, draftId: DraftId))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(WhoAreYouAgentPage, value))
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(WhoAreYouAgentPage, mode, updatedAnswers)
              )
          )
    }
}
