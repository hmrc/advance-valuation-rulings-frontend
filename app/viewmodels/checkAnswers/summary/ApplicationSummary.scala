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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.{TraderDetailsWithCountryCode, UserAnswers}
import models.AuthUserType._
import userrole.UserRoleProvider

case class ApplicationSummary(
  eoriDetails: EoriDetailsSummary,
  applicant: ApplicantSummary,
  details: DetailsSummary,
  method: MethodSummary
)

class ApplicationSummaryService @Inject() (
  frontendAppConfig: FrontendAppConfig,
  userRoleProvider: UserRoleProvider
) {

  private val logger = Logger(this.getClass)

  def getApplicationSummary(
    userAnswers: UserAnswers,
    traderDetailsWithCountryCode: TraderDetailsWithCountryCode
  )(implicit
    messages: Messages
  ): ApplicationSummary = {
    val (applicant, company) = userRoleProvider
      .getUserRole(userAnswers)
      .getApplicationSummary(userAnswers, traderDetailsWithCountryCode)
    ApplicationSummary(
      eoriDetails = company,
      applicant = applicant,
      details = DetailsSummary(userAnswers),
      method = MethodSummary(userAnswers)
    )
  }
}
