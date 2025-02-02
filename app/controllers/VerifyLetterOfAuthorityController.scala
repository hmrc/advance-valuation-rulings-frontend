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

import controllers.actions._
import controllers.routes.{JourneyRecoveryController, UploadLetterOfAuthorityController}
import models._
import navigation.Navigator
import pages.{UploadLetterOfAuthorityPage, VerifyLetterOfAuthorityPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VerifyLetterOfAuthorityView

import javax.inject.Inject

class VerifyLetterOfAuthorityController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: VerifyLetterOfAuthorityView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      UploadLetterOfAuthorityPage.get() match {
        case Some(attachment) =>
          attachment.fileName match {
            case Some(_) =>
              Ok(view(attachment, draftId, mode))
            case None    =>
              Redirect(
                UploadLetterOfAuthorityController.onPageLoad(
                  mode,
                  draftId,
                  None,
                  None,
                  redirectedFromChangeButton = false
                )
              )
          }
        case None             => Redirect(JourneyRecoveryController.onPageLoad())
      }

    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      Redirect(navigator.nextPage(VerifyLetterOfAuthorityPage, mode, request.userAnswers))
    }
}
