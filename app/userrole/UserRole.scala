package userrole

import models.requests.DataRequest
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import play.twirl.api.HtmlFormat

trait UserRole {

  def selectViewForCheckRegDetails(
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
