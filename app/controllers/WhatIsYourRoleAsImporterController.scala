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

import audit.AuditService
import controllers.actions._
import forms.WhatIsYourRoleAsImporterFormProvider
import models.WhatIsYourRoleAsImporter._
import models.requests.DataRequest
import models.{CheckMode, DraftId, Mode, NormalMode, WhatIsYourRoleAsImporter}
import navigation.{Navigator, UnchangedModeNavigator}
import pages.{ChangeYourRoleImporterPage, DraftWhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporterPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.WhatIsYourRoleAsImporterView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourRoleAsImporterController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  unchangedModeNavigator: UnchangedModeNavigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  auditService: AuditService,
  formProvider: WhatIsYourRoleAsImporterFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsYourRoleAsImporterView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private[controllers] def onSubmitNavigationLogic(
    formValue: WhatIsYourRoleAsImporter,
    mode: Mode
  )(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] = {

    val whatIsYourRoleAnswers = request.userAnswers.get(WhatIsYourRoleAsImporterPage)

    (formValue, mode, whatIsYourRoleAnswers) match {
      case (formAnswer, CheckMode, Some(role)) if formAnswer == role  =>
        for {
          ua <- request.userAnswers.setFuture(WhatIsYourRoleAsImporterPage, formValue)
          _  <- userAnswersService.set(ua)
        } yield Redirect(
          unchangedModeNavigator.nextPage(WhatIsYourRoleAsImporterPage, ua)
        )
      case (_, NormalMode, None)                                      =>
        for {
          ua <- request.userAnswers.setFuture(WhatIsYourRoleAsImporterPage, formValue)
          _  <- userAnswersService.set(ua)
        } yield Redirect(
          navigator.nextPage(WhatIsYourRoleAsImporterPage, NormalMode, ua)
        )
      case (formAnswer, NormalMode, Some(role)) if formAnswer == role =>
        for {
          ua <- request.userAnswers.setFuture(WhatIsYourRoleAsImporterPage, formValue)
          _  <- userAnswersService.set(ua)
        } yield Redirect(
          navigator.nextPage(WhatIsYourRoleAsImporterPage, NormalMode, ua)
        )
      case (formAnswer, NormalMode, Some(role)) if formAnswer != role =>
        for {
          ua             <- request.userAnswers.setFuture(DraftWhatIsYourRoleAsImporterPage, formValue)
          updatedAnswers <- ua.removeFuture(ChangeYourRoleImporterPage)
          _              <- userAnswersService.set(updatedAnswers)
        } yield Redirect(
          navigator.nextPage(ChangeYourRoleImporterPage, NormalMode, updatedAnswers)
        )
      case _                                                          =>
        auditService.sendRoleIndicatorEvent(formValue)
        for {
          ua             <- request.userAnswers.setFuture(DraftWhatIsYourRoleAsImporterPage, formValue)
          updatedAnswers <- ua.removeFuture(ChangeYourRoleImporterPage)
          _              <- userAnswersService.set(updatedAnswers)
        } yield Redirect(
          navigator.nextPage(DraftWhatIsYourRoleAsImporterPage, CheckMode, updatedAnswers)
        )
    }
  }

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      val preparedForm = WhatIsYourRoleAsImporterPage.fill(form)
      Ok(view(preparedForm, mode, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, draftId))),
          value => onSubmitNavigationLogic(value, mode)
        )
    }
}
