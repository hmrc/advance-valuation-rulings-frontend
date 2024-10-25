/*
 * Copyright 2024 HM Revenue & Customs
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

import javax.inject.Inject

import play.api.Logger

import models.{UserAnswers, WhatIsYourRoleAsImporter}
import pages.WhatIsYourRoleAsImporterPage

/** A class to give a [[UserRole]] given the answer to @link(WhatIsYourRoleAsImporterPage)
  */
class UserRoleProvider @Inject() (
  employeeRole: Employee,
  agentForOrg: AgentForOrg,
  agentForTrader: AgentForTrader
) {

  private given logger: Logger = Logger(this.getClass)

  def getUserRole(userAnswers: UserAnswers): UserRole =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case Some(WhatIsYourRoleAsImporter.EmployeeOfOrg)         =>
        employeeRole
      case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)    =>
        agentForOrg
      case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader) =>
        agentForTrader
      case _                                                    =>
        logger.error(
          "[UserRoleProvider][getUserRole] WhatIsYourRoleAsImporterPage should have been answered before calling UserRoleProvider.getUserRole"
        )
        throw new UnsupportedOperationException(
          "WhatIsYourRoleAsImporterPage should have been answered before calling UserRoleProvider.getUserRole"
        )

    }

}
