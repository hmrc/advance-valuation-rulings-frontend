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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import forms.CancelApplicationFormProvider
import models.{DraftId, NormalMode}
import navigation.Navigator
import pages.CancelApplicationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CancelAreYouSureView
import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}

class CancelApplicationController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  userAnswersService: UserAnswersService,
  val controllerComponents: MessagesControllerComponents,
  formProvider: CancelApplicationFormProvider,
  view: CancelAreYouSureView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  val form: Form[Boolean]                              = formProvider()
  // @nowarn("cat=unused")
  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      Ok(view(form, draftId))
    }

  def confirmCancel(draftId: DraftId): Action[AnyContent] =
    identify.async { implicit request =>
      for {
        _ <- userAnswersService.clear(draftId)
      } yield Redirect(controllers.routes.AccountHomeController.onPageLoad())
    }

  def onSubmit(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, draftId))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers
                    .set(CancelApplicationPage, value)
                )
              _              <- userAnswersService.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(CancelApplicationPage, NormalMode, updatedAnswers)
            )
        )
    }
}
