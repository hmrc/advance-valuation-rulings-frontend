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

import models.UserAnswers
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.TryValues
import pages._

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(UploadAnotherSupportingDocumentPage.type, JsValue)] ::
      arbitrary[(IsThisFileConfidentialPage.type, JsValue)] ::
      arbitrary[(DoYouWantToUploadDocumentsPage.type, JsValue)] ::
      arbitrary[(ApplicationContactDetailsPage.type, JsValue)] ::
      arbitrary[(WhatCountryAreGoodsFromPage.type, JsValue)] ::
      arbitrary[(AreGoodsShippedDirectlyPage.type, JsValue)] ::
      arbitrary[(CheckRegisteredDetailsPage.type, JsValue)] ::
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
