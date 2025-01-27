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

import connectors.BackendConnector
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import controllers.common.TraderDetailsHelper
import forms.ChangeYourRoleImporterFormProvider
import models.{DraftId, Mode, NormalMode, WhatIsYourRoleAsImporter}
import navigation.Navigator
import pages.{ChangeYourRoleImporterPage, DraftWhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporterPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DeleteAllUserAnswersService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangeYourRoleImporterView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeYourRoleImporterController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  requireData: DataRequiredAction,
  getData: DataRetrievalActionProvider,
  formProvider: ChangeYourRoleImporterFormProvider,
  navigator: Navigator,
  deleteAllUserAnswersService: DeleteAllUserAnswersService,
  view: ChangeYourRoleImporterView,
  userAnswersService: UserAnswersService,
  implicit val backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      val form: Form[Boolean] = formProvider()
      val filledForm          = ChangeYourRoleImporterPage.fill(form)
      Future(
        Ok(
          view(
            filledForm,
            draftId,
            controllers.routes.ChangeYourRoleImporterController.onSubmit(mode, draftId)
          )
        )
      )
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  draftId,
                  controllers.routes.ChangeYourRoleImporterController.onSubmit(mode, draftId)
                )
              )
            ),
          success = {
            case true  =>
              val updateUserAnswersAndRedirect =
                for {
                  getAnswersOpt: Option[WhatIsYourRoleAsImporter] <-
                    Future(request.userAnswers.get(DraftWhatIsYourRoleAsImporterPage))
                  _                                               <- userAnswersService.clear(draftId)
                  setRoleFromDraft                                <- getAnswersOpt.fold(Future(request.userAnswers))(role =>
                                                                       request.userAnswers.setFuture(WhatIsYourRoleAsImporterPage, role)
                                                                     )
                  saveChangeRoleAnswer                            <- setRoleFromDraft.setFuture(ChangeYourRoleImporterPage, true)
                  deletedUaFromData                               <- Future(
                                                                       deleteAllUserAnswersService
                                                                         .deleteAllUserAnswersExcept(
                                                                           saveChangeRoleAnswer,
                                                                           Seq(WhatIsYourRoleAsImporterPage, ChangeYourRoleImporterPage)
                                                                         )
                                                                         .getOrElse(request.userAnswers)
                                                                     )
                  _                                               <- userAnswersService.set(deletedUaFromData)
                } yield Redirect(
                  navigator.nextPage(ChangeYourRoleImporterPage, NormalMode, deletedUaFromData)
                )
              updateUserAnswersAndRedirect
            case false =>
              for {
                ua <- request.userAnswers.setFuture(ChangeYourRoleImporterPage, false)
                _  <- userAnswersService.set(ua)
              } yield Redirect(
                navigator.nextPage(ChangeYourRoleImporterPage, mode, ua)
              )
          }
        )
    }
}
