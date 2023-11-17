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
import models.DraftId
import pages.VerifyTraderDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import userrole.UserRoleProvider

import javax.inject.Inject

class EORIBeUpToDateController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  userRoleProvider: UserRoleProvider
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      val isPrivate = VerifyTraderDetailsPage.get() match {
        case Some(details) => !details.consentToDisclosureOfPersonalData
        case _             => true
      }
      Ok(
        userRoleProvider
          .getUserRole(request.userAnswers)
          .selectViewForEoriBeUpToDate(draftId, isPrivate)
      )
    }
}
