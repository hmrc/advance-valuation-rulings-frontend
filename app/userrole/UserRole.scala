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

package userrole

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat

import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import models.requests.DataRequest

trait UserRole {

  def selectViewForCheckRegisteredDetails(
    form: Form[Boolean],
    details: TraderDetailsWithCountryCode,
    mode: Mode,
    draftId: DraftId
  )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable

  //  def apply(userAnswers: UserAnswers): UserRole =
  //    userAnswers.get(AccountHomePage) match {
  //      case Some(AuthUserType.IndividualTrader)                                 =>
  //        Employee
  //      case Some(AuthUserType.OrganisationAdmin)                                =>
  //        AgentForOrg
  //      case Some(AuthUserType.OrganisationAssistant) | Some(AuthUserType.Agent) =>
  //        userAnswers.get(WhatIsYourRoleAsImporterPage) match {
  //          case Some(WhatIsYourRoleAsImporter.EmployeeOfOrg)      =>
  //            AgentForTrader
  //          case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg) =>
  //            AgentForTrader
  //          case _                                                 =>
  //            AgentForTrader
  //        }
  //      case _                                                                   =>
  //        AgentForTrader
  //    }

}
