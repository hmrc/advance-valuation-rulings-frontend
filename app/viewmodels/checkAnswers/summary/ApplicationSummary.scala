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

package viewmodels.checkAnswers.summary

import play.api.Logger
import play.api.i18n.Messages
import uk.gov.hmrc.auth.core.InsufficientEnrolments

import models.AuthUserType._
import models.TraderDetailsWithCountryCode
import models.UserAnswers
import pages.AccountHomePage

case class ApplicationSummary(
  eoriDetails: EoriDetailsSummary,
  applicant: ApplicantSummary,
  details: DetailsSummary,
  method: MethodSummary
)

object ApplicationSummary {

  private val logger = Logger(this.getClass)

  def apply(
    userAnswers: UserAnswers,
    traderDetailsWithCountryCode: TraderDetailsWithCountryCode
  )(implicit
    messages: Messages
  ): ApplicationSummary = {
    val (applicant, company) = userAnswers.get(AccountHomePage) match {
      case Some(IndividualTrader) | Some(OrganisationAdmin) =>
        (
          IndividualApplicantSummary(userAnswers),
          IndividualEoriDetailsSummary(traderDetailsWithCountryCode, userAnswers.draftId)
        )
      case Some(OrganisationAssistant)                      =>
        (
          AgentSummary(userAnswers),
          BusinessEoriDetailsSummary(traderDetailsWithCountryCode, userAnswers.draftId)
        )
      case unexpected                                       =>
        logger.error(s"Unsupported authUserType [$unexpected] encountered")
        throw InsufficientEnrolments("Unexpected authUserType")
    }

    ApplicationSummary(
      eoriDetails = company,
      applicant = applicant,
      details = DetailsSummary(userAnswers),
      method = MethodSummary(userAnswers)
    )
  }
}
