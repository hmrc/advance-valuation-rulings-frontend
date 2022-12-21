package generators

import play.api.libs.json.{Json, JsValue}

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryPriceOfGoodsUserAnswersEntry
    : Arbitrary[(PriceOfGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PriceOfGoodsPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowAreTheGoodsMadeUserAnswersEntry
    : Arbitrary[(HowAreTheGoodsMadePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HowAreTheGoodsMadePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasConfidentialInformationUserAnswersEntry
    : Arbitrary[(HasConfidentialInformationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasConfidentialInformationPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDescribeTheGoodsUserAnswersEntry
    : Arbitrary[(DescribeTheGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DescribeTheGoodsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryConfidentialInformationUserAnswersEntry
    : Arbitrary[(ConfidentialInformationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ConfidentialInformationPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRequiredInformationUserAnswersEntry
    : Arbitrary[(RequiredInformationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RequiredInformationPage.type]
        value <- arbitrary[RequiredInformation].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryImportGoodsUserAnswersEntry
    : Arbitrary[(ImportGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ImportGoodsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasCommodityCodeUserAnswersEntry
    : Arbitrary[(HasCommodityCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasCommodityCodePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCommodityCodeUserAnswersEntry
    : Arbitrary[(CommodityCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CommodityCodePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryValuationMethodUserAnswersEntry
    : Arbitrary[(ValuationMethodPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ValuationMethodPage.type]
        value <- arbitrary[ValuationMethod].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryNameOfGoodsUserAnswersEntry
    : Arbitrary[(NameOfGoodsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NameOfGoodsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }
}
