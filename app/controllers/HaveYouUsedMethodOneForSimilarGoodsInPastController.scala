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

import controllers.actions._
import forms.HaveYouUsedMethodOneForSimilarGoodsInPastFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.HaveYouUsedMethodOneForSimilarGoodsInPastPage
import repositories.SessionRepository
import views.html.HaveYouUsedMethodOneForSimilarGoodsInPastView

class HaveYouUsedMethodOneForSimilarGoodsInPastController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: HaveYouUsedMethodOneForSimilarGoodsInPastFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: HaveYouUsedMethodOneForSimilarGoodsInPastView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm =
        request.userAnswers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value => {
              val userAnswers =
                if (value) request.userAnswers
                else UserAnswers.clearValuationMethod(request.userAnswers)
              for {
                ua <- userAnswers.setFuture(HaveYouUsedMethodOneForSimilarGoodsInPastPage, value)
                _  <- sessionRepository.set(ua)
              } yield Redirect(
                navigator.nextPage(HaveYouUsedMethodOneForSimilarGoodsInPastPage, mode, ua)(
                  request.affinityGroup
                )
              )
            }
          )
    }
}
