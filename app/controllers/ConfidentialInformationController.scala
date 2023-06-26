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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import forms.ConfidentialInformationFormProvider
import models.{DraftId, Mode}
import navigation.Navigator
import pages.ConfidentialInformationPage
import services.UserAnswersService
import views.html.ConfidentialInformationView

class ConfidentialInformationController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  actions: Actions,
  formProvider: ConfidentialInformationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfidentialInformationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String]                                           = formProvider()
  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (actions.identifyDraft(draftId)) {
      implicit request =>
        val preparedForm = ConfidentialInformationPage.fill(form)
        Ok(view(preparedForm, mode, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (actions.identifyWithHistory(draftId, ConfidentialInformationPage)).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, draftId))),
            value =>
              for {
                updatedAnswers <- ConfidentialInformationPage.set(value)
                _              <- userAnswersService.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(ConfidentialInformationPage, mode, updatedAnswers)
              )
          )
    }
}
