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

import scala.concurrent.Future

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import controllers.common.FileUploadHelper
import models._
import pages._
import views.html.UploadLetterOfAuthorityView

@Singleton
class UploadLetterOfAuthorityController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  view: UploadLetterOfAuthorityView,
  helper: FileUploadHelper
) extends FrontendBaseController
    with I18nSupport {

  private val mode: Mode                       = NormalMode // TODO: allow other modes other than NormalMode.
  private val controller                       = controllers.routes.UploadLetterOfAuthorityController
  private val page: QuestionPage[UploadedFile] = UploadLetterOfAuthorityPage

  def onPageLoad(
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String]
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val redirectPath =
          controller.onPageLoad(draftId, None, None).url
        val answers      = request.userAnswers
        answers
          .get(page)
          .map {
            case file: UploadedFile.Initiated =>
              errorCode
                .map(
                  errorCode =>
                    helper.showErrorPage(
                      draftId,
                      helper.errorForCode(errorCode),
                      redirectPath,
                      isLetterOfAuthority = true
                    )
                )
                .getOrElse {
                  if (key.contains(file.reference)) {
                    showInterstitialPage(draftId)
                  } else {
                    helper.showPage(draftId, redirectPath, isLetterOfAuthority = true)
                  }
                }
            case file: UploadedFile.Success   =>
              if (key.contains(file.reference)) {
                helper.continue(mode, answers, UploadLetterOfAuthorityPage)
              } else {
                helper.showPage(draftId, redirectPath, isLetterOfAuthority = true)
              }
            case file: UploadedFile.Failure   =>
              helper.redirectWithError(
                draftId,
                key,
                file.failureDetails.failureReason.toString,
                redirectPath,
                isLetterOfAuthority = true,
                NormalMode // TODO support modes?
              )
          }
          .getOrElse {
            helper.showPage(draftId, redirectPath, isLetterOfAuthority = true)
          }
    }

  private def showInterstitialPage(
    draftId: DraftId
  )(implicit request: RequestHeader): Future[Result] =
    Future.successful(
      Ok(
        view(
          draftId = draftId,
          upscanInitiateResponse = None,
          errorMessage = None
        )
      )
    )

}
