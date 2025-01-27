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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.{UserAnswers, WhatIsYourRoleAsImporter}
import pages.WhatIsYourRoleAsImporterPage

sealed abstract class WhatIsYourRole(override val entryName: String) extends EnumEntry

object WhatIsYourRole extends Enum[WhatIsYourRole] with PlayJsonEnum[WhatIsYourRole] {
  val values: IndexedSeq[WhatIsYourRole] = findValues

  def apply(userAnswers: UserAnswers): WhatIsYourRole =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case Some(WhatIsYourRoleAsImporter.EmployeeOfOrg)         => EmployeeOrg
      case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg)    => AgentOrg
      case Some(WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader) => AgentTrader
      case None                                                 => UnansweredLegacySupport
    }

  case object EmployeeOrg extends WhatIsYourRole("EmployeeOrg")

  case object AgentOrg extends WhatIsYourRole("AgentOrg")

  case object AgentTrader extends WhatIsYourRole("AgentTrader")

  private case object UnansweredLegacySupport extends WhatIsYourRole("UnansweredLegacySupport")
}
