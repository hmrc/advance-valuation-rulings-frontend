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

package pages

import play.api.libs.json.JsPath
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialRole, User}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

import controllers.routes
import controllers.routes._
import models.{BusinessContactDetails, CheckMode, Mode, NormalMode, UserAnswers}
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import navigation.resolveAffinityGroup

case object BusinessContactDetailsPage extends QuestionPage[BusinessContactDetails] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "businessContactDetails"

  def nextPage(
    mode: Mode,
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup,
    credentialRole: Option[CredentialRole]
  ): Call =
    if (affinityGroup == Organisation && credentialRole.nonEmpty) {
      mode match {
        case NormalMode =>
          businessContactDetailsPage(userAnswers, credentialRole)
        case CheckMode  =>
          resolveAffinityGroup(affinityGroup)(
            routes.CheckYourAnswersController.onPageLoad(userAnswers.draftId),
            routes.CheckYourAnswersForAgentsController.onPageLoad(userAnswers.draftId)
          )
      }
    } else {
      routes.JourneyRecoveryController.onPageLoad()
    }

  private def businessContactDetailsPage(
    userAnswers: UserAnswers,
    credentialRole: Option[CredentialRole]
  ): Call =
    userAnswers.get(BusinessContactDetailsPage) match {
      case None    => BusinessContactDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(_) => businessAgentContactDetailsNavigation(userAnswers, credentialRole)
    }

  private def businessAgentContactDetailsNavigation(
    userAnswers: UserAnswers,
    credentialRole: Option[CredentialRole]
  ): Call =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case None if credentialRole.contains(User) =>
        ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(EmployeeOfOrg)                   =>
        ValuationMethodController.onPageLoad(NormalMode, userAnswers.draftId)
      case Some(AgentOnBehalfOfOrg)              =>
        AgentCompanyDetailsController.onPageLoad(NormalMode, userAnswers.draftId)
      case _                                     =>
        WhatIsYourRoleAsImporterController.onPageLoad(NormalMode, userAnswers.draftId)
    }
}
