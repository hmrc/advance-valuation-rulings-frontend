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

import play.api.Configuration
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import com.google.inject.Inject
import config.FrontendAppConfig

sealed trait WhoAreYouAgent

class WhoAreYouAgents @Inject() (configuration: FrontendAppConfig) {

  val traderDetailsCacheEnabledValue = configuration.traderDetailsCacheEnabled

}

object WhoAreYouAgent extends Enumerable.Implicits {

  case object OrganisationEmployee extends WithName("organisationEmployee") with WhoAreYouAgent
  case object AgentOnBehalfOfOrganisation
      extends WithName("agentOnBehalfOfOrganisation")
      with WhoAreYouAgent
  case object AgentOnBehalfOfTrader extends WithName("agentOnBehalfOfTrader") with WhoAreYouAgent

  val values: Seq[WhoAreYouAgent] =
    Seq(
      OrganisationEmployee,
      AgentOnBehalfOfOrganisation,
      AgentOnBehalfOfTrader
    )

  def filteredValues(appConfig: FrontendAppConfig): Seq[WhoAreYouAgent] =
    if (appConfig.traderDetailsCacheEnabled) values
    else values.filterNot(_ == AgentOnBehalfOfTrader)

  def options(appConfig: FrontendAppConfig)(implicit messages: Messages): Seq[RadioItem] =
    filteredValues(appConfig).zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"whoAreYouAgent.${value.toString}")),
          hint =
            Some(Hint(content = HtmlContent(messages(s"whoAreYouAgent.${value.toString}.hint")))),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }

  implicit val enumerable: Enumerable[WhoAreYouAgent] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
