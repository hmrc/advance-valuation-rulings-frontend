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

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import controllers.actions._
import models.UserAnswers
import pages.{ApplicationContactDetailsPage, BusinessContactDetailsPage}
import viewmodels.checkAnswers.summary.ApplicationSummary
import views.html.ApplicationCompleteView

class ApplicationCompleteController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ApplicationCompleteView
) extends FrontendBaseController
    with I18nSupport {

  private val logger = Logger(this.getClass)

  def onPageLoad(applicationNumber: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val answers            = request.userAnswers
        val applicationSummary = ApplicationSummary(answers, request.affinityGroup).removeActions()

        extractApplicantEmail(answers, request.affinityGroup) match {
          case Some(JsString(applicantEmail)) =>
            Ok(view(applicationNumber, applicantEmail, applicationSummary))
          case _                              =>
            logger.error(s"Applicant email is empty for id: ${request.userId}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
    }

  private def extractApplicantEmail(
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): Option[JsValue] = {
    val contactDetailsFieldName =
      if (affinityGroup == Individual) {
        ApplicationContactDetailsPage.toString
      } else {
        BusinessContactDetailsPage.toString
      }
    (userAnswers.data \ contactDetailsFieldName \ "email").toOption
  }
}
