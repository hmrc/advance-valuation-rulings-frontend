package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryRequiredInformationUserAnswersEntry: Arbitrary[(RequiredInformationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RequiredInformationPage.type]
        value <- arbitrary[RequiredInformation].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImportGoodsUserAnswersEntry: Arbitrary[(ImportGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImportGoodsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasCommodityCodeUserAnswersEntry: Arbitrary[(HasCommodityCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasCommodityCodePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCommodityCodeUserAnswersEntry: Arbitrary[(CommodityCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CommodityCodePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryValuationMethodUserAnswersEntry: Arbitrary[(ValuationMethodPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ValuationMethodPage.type]
        value <- arbitrary[ValuationMethod].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNameOfGoodsUserAnswersEntry: Arbitrary[(NameOfGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NameOfGoodsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }
}
