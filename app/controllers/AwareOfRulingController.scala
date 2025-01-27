/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import forms.AwareOfRulingFormProvider
import models.{DraftId, Mode}
import navigation.Navigator
import pages.AwareOfRulingPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AwareOfRulingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AwareOfRulingController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: AwareOfRulingFormProvider,
  view: AwareOfRulingView,
  navigator: Navigator,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      val preparedForm =
        request.userAnswers.get(AwareOfRulingPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

      Ok(view(preparedForm, mode, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, draftId))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(AwareOfRulingPage, value))
              _              <- userAnswersService.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(AwareOfRulingPage, mode, updatedAnswers)
            )
        )
    }

}
