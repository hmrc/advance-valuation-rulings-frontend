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

import models.AuthUserType
import models.AuthUserType._
import play.api.mvc.Call

package object navigation {

  def resolveAuthUserType(authUserType: AuthUserType)(
    isTrader: Call,
    isEmployee: Call,
    isAgent: Call
  ): Call =
    authUserType match {
      case IndividualTrader      => isTrader
      case OrganisationUser      => isEmployee
      case OrganisationAssistant => isAgent
      case Agent                 => isAgent
    }
}
