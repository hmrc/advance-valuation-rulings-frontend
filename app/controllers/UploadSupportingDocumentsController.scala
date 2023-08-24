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

import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import controllers.common.FileUploadHelper
import models._
import pages._

@Singleton
class UploadSupportingDocumentsController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  helper: FileUploadHelper
) extends FrontendBaseController
    with I18nSupport {

  private val controller = controllers.routes.UploadSupportingDocumentsController

  def onPageLoad(
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String]
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val redirectPath = controller.onPageLoad(mode, draftId, None, None).url
        val answers      = request.userAnswers
        answers
          .get(UploadSupportingDocumentPage)
          .map {
            case file: UploadedFile.Initiated =>
              errorCode
                .map(
                  errorCode =>
                    helper.showErrorPage(
                      draftId,
                      helper.errorForCode(errorCode),
                      redirectPath,
                      isLetterOfAuthority = false
                    )
                )
                .getOrElse {
                  if (key.contains(file.reference)) {
                    helper.showInterstitialPage(draftId)
                  } else {
                    helper.showPage(draftId, redirectPath, isLetterOfAuthority = false)
                  }
                }
            case file: UploadedFile.Success   =>
              if (key.contains(file.reference)) {
                helper.continue(mode, answers, UploadSupportingDocumentPage)
              } else {
                helper.showPage(draftId, redirectPath, isLetterOfAuthority = false)
              }
            case file: UploadedFile.Failure   =>
              helper.redirectWithError(
                draftId,
                key,
                file.failureDetails.failureReason.toString,
                redirectPath,
                isLetterOfAuthority = false,
                mode
              )
          }
          .getOrElse {
            helper.showPage(draftId, redirectPath, isLetterOfAuthority = false)
          }
    }

}
