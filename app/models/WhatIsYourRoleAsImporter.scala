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

package models

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import config.FrontendAppConfig

sealed trait WhatIsYourRoleAsImporter

object WhatIsYourRoleAsImporter extends Enumerable.Implicits {

  val MessagePrefix = "whatIsYourRoleAsImporter"

  case object EmployeeOfOrg extends WithName("employeeOfOrg") with WhatIsYourRoleAsImporter
  case object AgentOnBehalfOfOrg
      extends WithName("agentOnBehalfOfOrg")
      with WhatIsYourRoleAsImporter

  case object AgentOnBehalfOfTrader
      extends WithName("agentOnBehalfOfTrader")
      with WhatIsYourRoleAsImporter

  val values: Seq[WhatIsYourRoleAsImporter] = Seq(
    EmployeeOfOrg,
    AgentOnBehalfOfOrg,
    AgentOnBehalfOfTrader
  )

  def filteredValues(appConfig: FrontendAppConfig): Seq[WhatIsYourRoleAsImporter] =
    if (appConfig.agentOnBehalfOfTrader) values
    else values.filterNot(_ == AgentOnBehalfOfTrader)
  def options(appConfig: FrontendAppConfig, shouldRadiosBeDisabled: Boolean)(implicit
    messages: Messages
  ): Seq[RadioItem] =
    filteredValues(appConfig).zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = HtmlContent(
            Html(s"<b>${messages(s"$MessagePrefix.${value.toString}")}</b>")
          ),
          value = Some(value.toString),
          id = Some(s"value_$index"),
          hint = Some(Hint(content = Text(messages(s"$MessagePrefix.${value.toString}.hint")))),
          disabled = shouldRadiosBeDisabled
        )
    }

  implicit val enumerable: Enumerable[WhatIsYourRoleAsImporter] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
