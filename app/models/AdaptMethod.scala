package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait AdaptMethod

object AdaptMethod extends Enumerable.Implicits {

  case object Method1 extends WithName("method1") with AdaptMethod
  case object Method2 extends WithName("method2") with AdaptMethod

  val values: Seq[AdaptMethod] = Seq(
    Method1, Method2
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"adaptMethod.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[AdaptMethod] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
