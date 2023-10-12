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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import _root_.config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalActionProvider, IdentifierAction}
import controllers.common.FileUploadHelper
import controllers.routes.UploadAnotherSupportingDocumentController
import models._
import models.requests.DataRequest
import pages._
import queries.AllDocuments
import userrole.UserRoleProvider
@Singleton
class UploadSupportingDocumentsController @Inject() (
  override val messagesApi: MessagesApi,
  override val controllerComponents: MessagesControllerComponents,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  helper: FileUploadHelper,
  userRoleProvider: UserRoleProvider,
  appConfig: FrontendAppConfig
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String]
  ): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        val numberOfAttachments = AllDocuments.get().getOrElse(List.empty).length

        if (appConfig.agentOnBehalfOfTrader) {
          if (
            numberOfAttachments >= userRoleProvider
              .getUserRole(request.userAnswers)
              .getMaxSupportingDocuments
          ) {
            Future.successful(
              Redirect(UploadAnotherSupportingDocumentController.onPageLoad(NormalMode, draftId))
            )
          } else {
            loadUsingFileUploadHelper(mode, draftId, errorCode, key, request)
          }
        } else {
          loadUsingFileUploadHelper(mode, draftId, errorCode, key, request)
        }

    }

  private def loadUsingFileUploadHelper(
    mode: Mode,
    draftId: DraftId,
    errorCode: Option[String],
    key: Option[String],
    request: DataRequest[AnyContent]
  )(implicit dataRequest: DataRequest[AnyContent], hc: HeaderCarrier) = {
    val answers                          = request.userAnswers
    val fileStatus: Option[UploadedFile] = answers.get(UploadSupportingDocumentPage)

    helper.onPageLoadWithFileStatus(
      mode,
      draftId,
      errorCode,
      key,
      fileStatus,
      isLetterOfAuthority = false
    )(dataRequest, hc)
  }
}
