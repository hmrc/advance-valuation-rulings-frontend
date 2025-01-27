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
import controllers.actions._
import models.requests.Application
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ApplicationViewModel
import views.html.ViewApplicationView

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewApplicationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  backendConnector: BackendConnector,
  val controllerComponents: MessagesControllerComponents,
  view: ViewApplicationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {
  import ViewApplicationController._

  def onPageLoad(applicationId: String): Action[AnyContent] =
    identify.async { implicit request =>
      backendConnector.getApplication(applicationId).map { (application: Application) =>
        val viewModel   = ApplicationViewModel(application)
        val lastUpdated = formatter.format(application.lastUpdated)
        Ok(view(viewModel, applicationId, lastUpdated))
      }
    }
}

object ViewApplicationController {
  val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("dd/MM/yyyy")
    .withZone(ZoneId.systemDefault())
}
