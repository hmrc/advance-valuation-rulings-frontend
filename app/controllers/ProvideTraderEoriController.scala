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
import controllers.common.TraderDetailsHelper
import forms.TraderEoriNumberFormProvider
import models.requests.DataRequest
import models.{DraftId, EoriNumber, Mode, NormalMode, TraderDetailsWithConfirmation, UserAnswers}
import navigation.Navigator
import pages.{ProvideTraderEoriPage, VerifyTraderDetailsPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{InvalidTraderEoriView, ProvideTraderEoriView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ProvideTraderEoriController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: TraderEoriNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  implicit val backendConnector: BackendConnector,
  provideTraderEoriView: ProvideTraderEoriView,
  invalidEoriView: InvalidTraderEoriView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TraderDetailsHelper {

  private val form: Form[String] = formProvider()

  private given logger: Logger = Logger(this.getClass)

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ProvideTraderEoriPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(provideTraderEoriView(preparedForm, mode, draftId))
    }

  def onSubmit(mode: Mode, draftId: DraftId, saveDraft: Boolean): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            if (saveDraft) {
              Future.successful(Redirect(routes.DraftHasBeenSavedController.onPageLoad(draftId)))
            } else {
              Future.successful(BadRequest(provideTraderEoriView(formWithErrors, mode, draftId)))
            },
          eoriInput =>
            request.userAnswers.set(ProvideTraderEoriPage, eoriInput.toUpperCase()) match {
              case Success(eoriAnswers) =>
                if (!saveDraft) {
                  proceed(eoriAnswers, eoriInput.toUpperCase().replace(" ", ""), mode, draftId)
                } else {
                  userAnswersService.set(eoriAnswers).map { _ =>
                    Redirect(routes.DraftHasBeenSavedController.onPageLoad(draftId))
                  }
                }

              case Failure(error) =>
                logger.warn(
                  s"[ProvideTraderEoriController][onSubmit] Unable to store ProvideTraderEoriPage. Error: $error"
                )
                Future
                  .successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
            }
        )
    }

  private def proceed(eoriAnswers: UserAnswers, eoriInput: String, mode: Mode, draftId: DraftId)(implicit
    hc: HeaderCarrier,
    dataRequest: DataRequest[AnyContent]
  ): Future[Result] =
    userAnswersService.set(eoriAnswers).flatMap { _ =>
      getTraderDetails(
        details =>
          eoriAnswers.set[TraderDetailsWithConfirmation](
            VerifyTraderDetailsPage,
            TraderDetailsWithConfirmation(details)
          ) match {
            case Success(traderAnswers) =>
              userAnswersService.set(traderAnswers).map { _ =>
                Redirect(
                  navigator.nextPage(
                    ProvideTraderEoriPage,
                    NormalMode,
                    traderAnswers
                  )
                )
              }
            case Failure(error)         =>
              logger.warn(
                s"[ProvideTraderEoriController][proceed] Unable to store VerifyTraderDetailsPage. Error: $error"
              )
              Future.successful(
                Redirect(
                  controllers.routes.JourneyRecoveryController.onPageLoad()
                )
              )
          },
        Some(Future.successful(NotFound(invalidEoriView(mode, draftId, eoriInput)))),
        Some(EoriNumber(eoriInput))
      )
    }
}
