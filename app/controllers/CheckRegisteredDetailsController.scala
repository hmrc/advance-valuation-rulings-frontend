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

import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import connectors.BackendConnector
import controllers.actions._
import forms.CheckRegisteredDetailsFormProvider
import models.{AcknowledgementReference, DraftId, EoriNumber, Mode}
import models.requests.DataRequest
import navigation.Navigator
import org.apache.commons.lang3.StringUtils
import pages.CheckRegisteredDetailsPage
import services.UserAnswersService
import views.html.CheckRegisteredDetailsView

class CheckRegisteredDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: CheckRegisteredDetailsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CheckRegisteredDetailsView,
  backendConnector: BackendConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(this.getClass)

  private val AckRefLength = 32
  private val AckRefPad    = "0"

  def onPageLoad(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request => ???
//        request.userAnswers.get(CheckRegisteredDetailsPage) match {
//          case Some(value) =>
//            ???
//            handleForm(draftId) {
//              (details: CheckRegisteredDetails) =>
//                val form =
//                  formProvider(request.affinityGroup, details.consentToDisclosureOfPersonalData)
//                Ok(view(form.fill(value.value), mode, details, request.affinityGroup, draftId))
//            }
//          case None        =>
//            backendConnector
//              .getTraderDetails(
//                AcknowledgementReference(
//                  StringUtils
//                    .rightPad(request.userAnswers.draftId.toString, AckRefLength, AckRefPad)
//                ),
//                EoriNumber(request.eoriNumber)
//              )
//              .flatMap {
//                case Right(traderDetails) =>
//                  for {
//                    answers <-
//                      request.userAnswers
//                        .setFuture(CheckRegisteredDetailsPage, traderDetails.details)
//                    _       <- userAnswersService.set(answers)
//                    form     = formProvider(
//                                 request.affinityGroup,
//                                 traderDetails.details.consentToDisclosureOfPersonalData
//                               )
//                  } yield Ok(
//                    view(form, mode, traderDetails.details, request.affinityGroup, draftId)
//                  )
//                case Left(backendError)   =>
//                  logger.error(s"Failed to get trader details from backend: $backendError")
//                  Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
//              }
//        }
    }

  def onSubmit(mode: Mode, draftId: DraftId): Action[AnyContent] =
    (identify andThen getData(draftId) andThen requireData).async {
      implicit request => ???
//        val checkRegisteredDetails: Option[CheckRegisteredDetails] =
//          request.userAnswers.get(CheckRegisteredDetailsPage)
//
//        checkRegisteredDetails match {
//          case None                    =>
//            logger.warn(s"Failed to submit check registered details as user has no answers")
//            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
//          case Some(registeredDetails) =>
//            val form: Form[Boolean] = formProvider(
//              request.affinityGroup,
//              registeredDetails.consentToDisclosureOfPersonalData
//            )
//
//            form
//              .bindFromRequest()
//              .fold(
//                formWithErrors =>
//                  handleForm(draftId)(
//                    (details: CheckRegisteredDetails) =>
//                      BadRequest(
//                        view(formWithErrors, mode, details, request.affinityGroup, draftId)
//                      )
//                  ),
//                value => {
//                  val updatedDetails = registeredDetails.copy(value = value)
//                  for {
//                    updatedAnswers <-
//                      request.userAnswers.setFuture(CheckRegisteredDetailsPage, updatedDetails)
//                    _              <- userAnswersService.set(updatedAnswers)
//                  } yield Redirect(
//                    navigator.nextPage(CheckRegisteredDetailsPage, mode, updatedAnswers)(
//                      request.affinityGroup
//                    )
//                  )
//                }
//              )
//        }
    }

//  private def handleForm(draftId: DraftId)(
//    detailsToResult: CheckRegisteredDetails => Result
//  )(implicit request: DataRequest[AnyContent]): Future[Result] =
//    for {
//      userAnswers <- userAnswersService.get(draftId)
//      details      = userAnswers.flatMap(_.get(CheckRegisteredDetailsPage))
//      result       = details match {
//                       case Some(registrationDetails) =>
//                         detailsToResult(registrationDetails)
//                       case None                      =>
//                         logger.error(s"User has no answer for CheckRegisteredDetails")
//                         Redirect(routes.JourneyRecoveryController.onPageLoad())
//                     }
//    } yield result
}
