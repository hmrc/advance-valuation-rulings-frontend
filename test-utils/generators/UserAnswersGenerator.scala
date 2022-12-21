package generators

import play.api.libs.json.{Json, JsValue}

import models.UserAnswers
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.TryValues
import pages._

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(PriceOfGoodsPage.type, JsValue)] ::
      arbitrary[(HowAreTheGoodsMadePage.type, JsValue)] ::
      arbitrary[(HasConfidentialInformationPage.type, JsValue)] ::
      arbitrary[(DescribeTheGoodsPage.type, JsValue)] ::
      arbitrary[(ConfidentialInformationPage.type, JsValue)] ::
      arbitrary[(RequiredInformationPage.type, JsValue)] ::
      arbitrary[(ImportGoodsPage.type, JsValue)] ::
      arbitrary[(HasCommodityCodePage.type, JsValue)] ::
      arbitrary[(CommodityCodePage.type, JsValue)] ::
      arbitrary[(ValuationMethodPage.type, JsValue)] ::
      arbitrary[(NameOfGoodsPage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id   <- nonEmptyString
        data <- generators match {
                  case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
                  case _   => Gen.mapOf(oneOf(generators))
                }
      } yield UserAnswers(
        id = id,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
