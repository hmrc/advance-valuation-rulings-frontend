package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryRequiredInformationPage: Arbitrary[RequiredInformationPage.type] =
    Arbitrary(RequiredInformationPage)

  implicit lazy val arbitraryImportGoodsPage: Arbitrary[ImportGoodsPage.type] =
    Arbitrary(ImportGoodsPage)

  implicit lazy val arbitraryHasCommodityCodePage: Arbitrary[HasCommodityCodePage.type] =
    Arbitrary(HasCommodityCodePage)

  implicit lazy val arbitraryCommodityCodePage: Arbitrary[CommodityCodePage.type] =
    Arbitrary(CommodityCodePage)

  implicit lazy val arbitraryValuationMethodPage: Arbitrary[ValuationMethodPage.type] =
    Arbitrary(ValuationMethodPage)

  implicit lazy val arbitraryNameOfGoodsPage: Arbitrary[NameOfGoodsPage.type] =
    Arbitrary(NameOfGoodsPage)
}
