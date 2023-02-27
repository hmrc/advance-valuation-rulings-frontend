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

import java.util.UUID
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json, JsSuccess}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import forms.CheckRegisteredDetailsFormProvider
import models.{CheckRegisteredDetails, Mode, UserAnswers}
import models.requests.{DataRequest, TraderDetailsRequest}
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

  private val logger = Logger(this.getClass)

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers.get(CheckRegisteredDetailsPage) match {
          case Some(value) =>
            handleForm((details: CheckRegisteredDetails) => Ok(view(form.fill(value), mode, details)))
          case None        =>
            backendConnector
              .getTraderDetails(TraderDetailsRequest(UUID.randomUUID().toString, request.userId))
              .flatMap {
                case Right(traderDetails) =>
                  val checkRegisteredDetails = traderDetails.details
                  val mergedUserAnswersData  =
                    request.userAnswers.data ++ Json.toJsObject(checkRegisteredDetails)

                  sessionRepository
                    .set(UserAnswers(request.userId, mergedUserAnswersData))
                    .map(_ => Ok(view(form, mode, checkRegisteredDetails)))
                case Left(backendError)   =>
                  logger.error(s"Failed to get trader details from backend: $backendError")
                  Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
              }
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              handleForm((details: CheckRegisteredDetails) => BadRequest(view(formWithErrors, mode, details))),
            value =>
              for {
                answers        <-
                  Future.fromTry(request.userAnswers.set(CheckRegisteredDetailsPage, value))
                updatedAnswers <- sessionRepository.update(answers)
              } yield Redirect(navigator.nextPage(CheckRegisteredDetailsPage, mode, updatedAnswers))
          )
    }

  private def handleForm(
    detailsToResult: CheckRegisteredDetails => Result
  )(implicit request: DataRequest[AnyContent]): Future[Result] =
    for {
      answers <- sessionRepository.get(request.userAnswers.id)
      result   = answers.map(_.data.validate[CheckRegisteredDetails]) match {
                   case Some(JsSuccess(registrationDetails, _)) =>
                     logger.debug(s"User answers data\n ${Json.prettyPrint(answers.get.data)}")
                     detailsToResult(registrationDetails)
                   case Some(JsError(error))                    =>
                     logger.error(s"Failed to convert json to CheckRegisteredDetails: $error")
                     Redirect(routes.JourneyRecoveryController.onPageLoad())
                 }
    } yield result
}
