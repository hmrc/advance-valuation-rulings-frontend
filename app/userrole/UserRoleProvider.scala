package userrole

import javax.inject.Inject

class UserRoleProvider @Inject()(employeeRole: Employee, agentForRole: AgentForOrg, agentFortTrader: AgentForTrader) {

  def getUserRole(): UserRole = employeeRole

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
