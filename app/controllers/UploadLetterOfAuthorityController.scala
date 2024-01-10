/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.common.FileUploadHelper
import models._
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.{Inject, Singleton}

@Singleton
class UploadLetterOfAuthorityController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  helper: FileUploadHelper
) extends FrontendBaseController
    with I18nSupport {

  private val isLetterOfAuthority = true

  def onPageLoad(
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String],
    redirectedFromChangeButton: Boolean = false
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      val answers    = request.userAnswers
      val fileStatus = answers.get(UploadLetterOfAuthorityPage)
      if (redirectedFromChangeButton) {
        helper.showFallbackPage(mode, draftId, isLetterOfAuthority)
      } else {
        fileStatus match {
          case Some(_: UploadedFile.Success) =>
            helper.continue(mode, answers, isLetterOfAuthority)
          case _                             =>
            helper.onPageLoadWithFileStatus(
              mode,
              draftId,
              errorCode,
              key,
              fileStatus,
              isLetterOfAuthority
            )
        }
      }
    }
}
