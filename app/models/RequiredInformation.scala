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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import viewmodels.govuk.checkbox._

sealed trait RequiredInformation

object RequiredInformation extends Enumerable.Implicits {

  private case object Option1 extends WithName("option1") with RequiredInformation
  private case object Option2 extends WithName("option2") with RequiredInformation
  private case object Option3 extends WithName("option3") with RequiredInformation
  private case object Option4 extends WithName("option4") with RequiredInformation
  private case object Option5 extends WithName("option5") with RequiredInformation
  private case object Option6 extends WithName("option6") with RequiredInformation

  val values: Seq[RequiredInformation] = Seq(
    Option1,
    Option2,
    Option3,
    Option4,
    Option5,
    Option6
  )

  def checkboxItems(authUserType: AuthUserType)(implicit messages: Messages): Seq[CheckboxItem] =
    values.zipWithIndex.map {
      case (value, index) =>
        val suffix = authUserType match {
          case AuthUserType.IndividualTrader      => "individual"
          case AuthUserType.OrganisationAssistant => "organisation"
          case AuthUserType.OrganisationAdmin     => "organisation"
          case AuthUserType.Agent                 => "organisation"
        }
        CheckboxItemViewModel(
          content = Text(messages(s"requiredInformation.${value.toString}.$suffix")),
          fieldId = "value",
          index = index,
          value = value.toString
        )
    }

  implicit val enumerable: Enumerable[RequiredInformation] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
