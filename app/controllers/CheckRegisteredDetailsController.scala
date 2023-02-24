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

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import forms.CheckRegisteredDetailsFormProvider
import models.{CheckRegisteredDetails, Mode}
import models.requests.TraderDetailsRequest
import navigation.Navigator
import pages.CheckRegisteredDetailsPage
import repositories.SessionRepository
import views.html.CheckRegisteredDetailsView

class CheckRegisteredDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: CheckRegisteredDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CheckRegisteredDetailsView,
  backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[CheckRegisteredDetails] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers.get(CheckRegisteredDetailsPage) match {
          case Some(value) => Future.successful(Ok(view(form.fill(value), mode)))
          case None        =>
            backendConnector.getTraderDetails(TraderDetailsRequest("test Ack", "AB1234567")).map { // TODO: Replace with actual values
              case Right(traderDetailsWithCountryCode) =>
                val formFields = Json.toJson(traderDetailsWithCountryCode.traderDetails)
                Ok(view(form.bind(formFields).discardingErrors, mode))
              case Left(backendError)                  => Redirect(routes.JourneyRecoveryController.onPageLoad())
            }
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              for {
                updatedAnswers <-
                  Future.fromTry(request.userAnswers.set(CheckRegisteredDetailsPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(CheckRegisteredDetailsPage, mode, updatedAnswers))
          )
    }
}
