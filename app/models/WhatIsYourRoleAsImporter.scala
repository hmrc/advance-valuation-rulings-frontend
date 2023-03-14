package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait WhatIsYourRoleAsImporter

object WhatIsYourRoleAsImporter extends Enumerable.Implicits {

  case object Employeeoforg extends WithName("employeeOfOrg") with WhatIsYourRoleAsImporter
  case object Agentonbehalfoforg extends WithName("agentOnBehalfOfOrg") with WhatIsYourRoleAsImporter

  val values: Seq[WhatIsYourRoleAsImporter] = Seq(
    Employeeoforg, Agentonbehalfoforg
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"whatIsYourRoleAsImporter.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[WhatIsYourRoleAsImporter] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
