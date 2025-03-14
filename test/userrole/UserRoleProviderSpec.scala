/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{DraftId, UserAnswers, WhatIsYourRoleAsImporter}
import org.mockito.Mockito.mock
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.WhatIsYourRoleAsImporterPage

class UserRoleProviderSpec extends AnyFreeSpec with Matchers {

  private val employeeRole   = mock(classOf[Employee])
  private val agentForOrg    = mock(classOf[AgentForOrg])
  private val agentForTrader = mock(classOf[AgentForTrader])
  private val userAnswers    = new UserAnswers("UserID", DraftId(1L))

  private val userRoleProvider: UserRoleProvider =
    new UserRoleProvider(employeeRole, agentForOrg, agentForTrader)

  "UserRoleProvider.getUserRole" - {
    "should return an Employee when the WhatIsYourRoleAsImporterPage answer is EmployeeOfOrg" in {
      val userAnswersForEmployee =
        userAnswers.set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.EmployeeOfOrg).get

      val actualRole = userRoleProvider.getUserRole(userAnswersForEmployee)

      actualRole mustBe employeeRole
    }

    "should return an AgentForOrgRole when the WhatIsYourRoleAsImporterPage answer is AgentForOrg" in {
      val userAnswersForEmployee =
        userAnswers
          .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)
          .get

      val actualRole = userRoleProvider.getUserRole(userAnswersForEmployee)

      actualRole mustBe agentForOrg
    }

    "should return an AgentForTraderRole when the WhatIsYourRoleAsImporterPage answer is AgentForTrader" in {
      val userAnswersForEmployee =
        userAnswers
          .set(WhatIsYourRoleAsImporterPage, WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader)
          .get

      val actualRole = userRoleProvider.getUserRole(userAnswersForEmployee)

      actualRole mustBe agentForTrader
    }

    "should throw an exception when the WhatIsYourRoleAsImporterPage has not been answered" in {
      assertThrows[UnsupportedOperationException] {
        userRoleProvider.getUserRole(userAnswers)
      }

    }
  }
}
