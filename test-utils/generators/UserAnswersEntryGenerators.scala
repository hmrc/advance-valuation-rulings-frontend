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

import play.api.libs.json.{Json, JsValue}

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryHasSupportingDocumentsUserAnswersEntry
    : Arbitrary[(HasSupportingDocumentsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasSupportingDocumentsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicationContactDetailsUserAnswersEntry
    : Arbitrary[(ApplicationContactDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicationContactDetailsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhatCountryAreGoodsFromUserAnswersEntry
    : Arbitrary[(WhatCountryAreGoodsFromPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WhatCountryAreGoodsFromPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAreGoodsShippedDirectlyUserAnswersEntry
    : Arbitrary[(AreGoodsShippedDirectlyPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AreGoodsShippedDirectlyPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCheckRegisteredDetailsUserAnswersEntry
    : Arbitrary[(CheckRegisteredDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CheckRegisteredDetailsPage.type]
        value <- arbitrary[CheckRegisteredDetails].map(Json.toJson(_))
      } yield (page, value)
    }

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
