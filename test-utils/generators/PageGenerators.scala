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

package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryCheckRegisteredDetailsPage
    : Arbitrary[CheckRegisteredDetailsPage.type] =
    Arbitrary(CheckRegisteredDetailsPage)

  implicit lazy val arbitraryPriceOfGoodsPage: Arbitrary[PriceOfGoodsPage.type] =
    Arbitrary(PriceOfGoodsPage)

  implicit lazy val arbitraryHowAreTheGoodsMadePage: Arbitrary[HowAreTheGoodsMadePage.type] =
    Arbitrary(HowAreTheGoodsMadePage)

  implicit lazy val arbitraryHasConfidentialInformationPage
    : Arbitrary[HasConfidentialInformationPage.type] =
    Arbitrary(HasConfidentialInformationPage)

  implicit lazy val arbitraryDescribeTheGoodsPage: Arbitrary[DescribeTheGoodsPage.type] =
    Arbitrary(DescribeTheGoodsPage)

  implicit lazy val arbitraryConfidentialInformationPage
    : Arbitrary[ConfidentialInformationPage.type] =
    Arbitrary(ConfidentialInformationPage)

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
