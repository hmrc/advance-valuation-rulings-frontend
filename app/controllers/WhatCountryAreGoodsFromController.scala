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
import forms.WhatCountryAreGoodsFromFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{NameOfGoodsPage, WhatCountryAreGoodsFromPage}
import repositories.SessionRepository
import views.html.WhatCountryAreGoodsFromView

class WhatCountryAreGoodsFromController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatCountryAreGoodsFromFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatCountryAreGoodsFromView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val whatCountryAreGoodsFrom =
        request.userAnswers.get(NameOfGoodsPage).getOrElse("No name of goods found")

      val preparedForm =
        request.userAnswers
          .get(WhatCountryAreGoodsFromPage) match {
          case None         => form
          case Some(answer) => form.fill(answer)
        }

      Ok(view(preparedForm, mode, whatCountryAreGoodsFrom))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val whatCountryAreGoodsFrom =
          request.userAnswers.get(NameOfGoodsPage).getOrElse("No name of goods found")

        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, mode, whatCountryAreGoodsFrom))
              ),
            answer =>
              for {
                updatedAnswers <- Future.fromTry(
                                    request.userAnswers
                                      .set(WhatCountryAreGoodsFromPage, answer)
                                  )
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(WhatCountryAreGoodsFromPage, mode, updatedAnswers)
              )
          )
    }
}
