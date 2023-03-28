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

import java.time.LocalDate
import javax.inject.Inject

import scala.concurrent.ExecutionContext

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import models.{ApplicationsAndRulingsResponse, UserAnswers}
import navigation.Navigator
import repositories.SessionRepository
import views.html.AccountHomeView

class AccountHomeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  generateApplicationNumber: ApplicationNumberGenerationAction,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  view: AccountHomeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  // represents the backend retrieval
  val applications: Option[Seq[ApplicationsAndRulingsResponse]] = Some(
    Seq(
      ApplicationsAndRulingsResponse(
        ref = "GBV01234567",
        nameOfGoods = "Socks",
        dateSubmitted = LocalDate.now(),
        application = None
      ),
      ApplicationsAndRulingsResponse(
        ref = "GBV01234568",
        nameOfGoods = "Shirts",
        dateSubmitted = LocalDate.now(),
        application = None
      )
    )
  )
  def onPageLoad: Action[AnyContent]                            =
    (identify andThen getData)(implicit request => Ok(view(applications)))
  def startApplication: Action[AnyContent]                      =
    (identify andThen getData andThen generateApplicationNumber).async {
      implicit request =>
        for {
          _ <-
            sessionRepository.set(
              request.userAnswers.getOrElse(
                UserAnswers(request.userId, request.applicationNumber.render)
              )
            )
        } yield Redirect(navigator.startApplicationRouting(request.affinityGroup))
    }
}
