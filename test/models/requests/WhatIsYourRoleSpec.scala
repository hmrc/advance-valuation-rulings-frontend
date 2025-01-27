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

package models.requests

import base.SpecBase
import models.WhatIsYourRoleAsImporter._
import models._
import models.requests.WhatIsYourRole._
import pages.WhatIsYourRoleAsImporterPage

class WhatIsYourRoleSpec extends SpecBase {

  "WhatIsYourRole" - {

    Seq(
      (EmployeeOfOrg, EmployeeOrg),
      (AgentOnBehalfOfOrg, AgentOrg),
      (AgentOnBehalfOfTrader, AgentTrader)
    ).foreach { case (roleAsImporter, expectedResult) =>
      s"must return the associated role for roleAsImporter $roleAsImporter" in {

        val userAnswers: UserAnswers = userAnswersAsIndividualTrader
          .set(WhatIsYourRoleAsImporterPage, roleAsImporter)
          .success
          .value

        WhatIsYourRole(userAnswers) mustBe expectedResult
      }
    }
  }
}
