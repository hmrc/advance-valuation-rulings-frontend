package pages

import play.api.libs.json.JsPath

case object DescribeTheSimilarGoodsPage extends QuestionPage[String] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "describeTheSimilarGoods"
}
