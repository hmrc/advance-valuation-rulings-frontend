package pages

import models.AdaptMethod
import play.api.libs.json.JsPath

case object AdaptMethodPage extends QuestionPage[AdaptMethod] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "adaptMethod"
}
