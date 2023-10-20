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

import scala.concurrent.{ExecutionContext, Future}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import audit.AuditService
import controllers.actions._
import forms.WhatIsYourRoleAsImporterFormProvider
import models.{DraftId, Mode, ReadOnlyMode}
import models.WhatIsYourRoleAsImporter._
import models.requests.DataRequest
import navigation.Navigator
import pages.{AgentCompanyDetailsPage, WhatIsYourRoleAsImporterPage}
import services.UserAnswersService
import views.html.WhatIsYourRoleAsImporterView

class WhatIsYourRoleAsImporterController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
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

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) {
      implicit request =>
        val preparedForm          = WhatIsYourRoleAsImporterPage.fill(form)
        val whatIsYourRoleAnswers = request.userAnswers.get(WhatIsYourRoleAsImporterPage)
        // If this page has already been answered then the mode should be read only.
        val modeForView           = whatIsYourRoleAnswers.map(_ => ReadOnlyMode).getOrElse(mode)

        Ok(view(preparedForm, modeForView, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request =>
        mode match {
          case ReadOnlyMode =>
            Future.successful(
              Redirect(
                navigator.nextPage(WhatIsYourRoleAsImporterPage, mode, request.userAnswers)
              )
            )
          case _            => updateUserAnswersSubmit(mode, draftId)
        }
    }

  private def updateUserAnswersSubmit(mode: Mode, draftId: DraftId)(implicit
    request: DataRequest[AnyContent]
  ) =
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, mode, draftId))
          ),
        value => {
          auditService.sendRoleIndicatorEvent(value)
          for {
            ua <- value match {
                    case EmployeeOfOrg         => AgentCompanyDetailsPage.remove()
                    case AgentOnBehalfOfOrg    =>
                      Future.successful(request.userAnswers)
                    case AgentOnBehalfOfTrader =>
                      Future.successful(request.userAnswers)
                  }
            ua <- ua.setFuture(WhatIsYourRoleAsImporterPage, value)
            _  <- userAnswersService.set(ua)
          } yield Redirect(
            navigator.nextPage(WhatIsYourRoleAsImporterPage, mode, ua)
          )
        }
      )

}
