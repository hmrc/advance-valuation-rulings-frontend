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

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import controllers.common.FileUploadHelper
import models.{DraftId, Mode, UploadedFile}
import pages.UploadSupportingDocumentPage
import views.html.UploadInProgressView

class UploadInProgressController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: UploadInProgressView,
  helper: FileUploadHelper
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request => Ok(view(draftId))
    }

  def checkProgress(mode: Mode, draftId: DraftId, key: Option[String]) =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val answers = request.userAnswers
        helper
          .checkForStatus(answers, UploadSupportingDocumentPage)
          .map {

//            case file: UploadedFile.Initiated =>
//              errorCode
//                .map(
//                  errorCode =>
//                    helper.showErrorPage(
//                      draftId,
//                      helper.errorForCode(errorCode),
//                      redirectPath,
//                      isLetterOfAuthority = false
//                    )
//                )
//                .getOrElse {
//                  if (key.contains(file.reference)) {
//                    helper.showInterstitialPage(draftId)
//                  } else {
//                    helper.showPage(mode, draftId, isLetterOfAuthority = false)
//                  }
//                }

            case file: UploadedFile.Success =>
              if (key.contains(file.reference)) {
                helper.continue(mode, answers, UploadSupportingDocumentPage)
              } else {
                helper.showPage(mode, draftId, isLetterOfAuthority = false)
              }

            case file: UploadedFile.Failure =>
              helper.redirectWithError(
                draftId,
                key,
                file.failureDetails.failureReason.toString,
                isLetterOfAuthority = false,
                mode
              )

          }
          .getOrElse(helper.showPage(mode, draftId, isLetterOfAuthority = true))
    }

}
