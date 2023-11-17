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
import forms.ApplicationContactDetailsFormProvider
import models.{ApplicationContactDetails, DraftId, Mode}
import navigation.Navigator
import pages.ApplicationContactDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ApplicationContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ApplicationContactDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ApplicationContactDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[ApplicationContactDetails] = formProvider()

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      val preparedForm = ApplicationContactDetailsPage.fill(form)

      Ok(view(preparedForm, mode, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId, saveDraft: Boolean): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            if (saveDraft) {
              Future.successful(Redirect(routes.DraftHasBeenSavedController.onPageLoad(draftId)))
            } else {
              Future.successful(BadRequest(view(formWithErrors, mode, draftId)))
            },
          value =>
            for {
              updatedAnswers <-
                request.userAnswers.setFuture(ApplicationContactDetailsPage, value)
              _              <- userAnswersService.set(updatedAnswers)
            } yield
              if (saveDraft) {
                Redirect(routes.DraftHasBeenSavedController.onPageLoad(draftId))
              } else {
                Redirect(
                  navigator.nextPage(ApplicationContactDetailsPage, mode, updatedAnswers)
                )
              }
        )
    }
}
