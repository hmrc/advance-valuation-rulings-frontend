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

package services.checkAnswers

import com.google.inject.Inject
import models.{TraderDetailsWithCountryCode, UserAnswers}
import play.api.i18n.Messages
import userrole.UserRoleProvider
import viewmodels.checkAnswers.summary.{ApplicationSummary, DetailsSummary, MethodSummary}

class ApplicationSummaryService @Inject() (
  userRoleProvider: UserRoleProvider
) {
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
