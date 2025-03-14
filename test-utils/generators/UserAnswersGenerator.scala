/*
 * Copyright 2025 HM Revenue & Customs
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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[?], JsValue)]] =
    arbitrary[(AgentCompanyDetailsPage.type, JsValue)] ::
      arbitrary[(BusinessContactDetailsPage.type, JsValue)] ::
      arbitrary[(HaveYouUsedMethodOneForSimilarGoodsInPastPage.type, JsValue)] ::
      arbitrary[(DescribeTheSimilarGoodsPage.type, JsValue)] ::
      arbitrary[(ExplainWhyYouHaveNotSelectedMethodOneToFivePage.type, JsValue)] ::
      arbitrary[(ExplainHowYouWillUseMethodSixPage.type, JsValue)] ::
      arbitrary[(AdaptMethodPage.type, JsValue)] ::
      arbitrary[(ExplainWhyYouHaveNotSelectedMethodOneToThreePage.type, JsValue)] ::
      arbitrary[(ExplainWhyYouChoseMethodFourPage.type, JsValue)] ::
      arbitrary[(DescribeTheIdenticalGoodsPage.type, JsValue)] ::
      arbitrary[(IsTheSaleSubjectToConditionsPage.type, JsValue)] ::
      arbitrary[(HaveTheGoodsBeenSubjectToLegalChallengesPage.type, JsValue)] ::
      arbitrary[(DescriptionOfGoodsPage.type, JsValue)] ::
      arbitrary[(DescribeTheLegalChallengesPage.type, JsValue)] ::
      arbitrary[(IsThereASaleInvolvedPage.type, JsValue)] ::
      arbitrary[(IsSaleBetweenRelatedPartiesPage.type, JsValue)] ::
      arbitrary[(ExplainHowPartiesAreRelatedPage.type, JsValue)] ::
      arbitrary[(WhyTransactionValueOfSimilarGoodsPage.type, JsValue)] ::
      arbitrary[(IsTheSaleSubjectToConditionsPage.type, JsValue)] ::
      arbitrary[(DescribeTheRestrictionsPage.type, JsValue)] ::
      arbitrary[(DescribeTheConditionsPage.type, JsValue)] ::
      arbitrary[(AreThereRestrictionsOnTheGoodsPage.type, JsValue)] ::
      arbitrary[(WhyTransactionValueOfSimilarGoodsPage.type, JsValue)] ::
      arbitrary[(HaveYouUsedMethodOneInPastPage.type, JsValue)] ::
      arbitrary[(WhyComputedValuePage.type, JsValue)] ::
      arbitrary[(WhyIdenticalGoodsPage.type, JsValue)] ::
      arbitrary[(WhyComputedValuePage.type, JsValue)] ::
      arbitrary[(ExplainReasonComputedValuePage.type, JsValue)] ::
      arbitrary[(UploadAnotherSupportingDocumentPage.type, JsValue)] ::
      arbitrary[(IsThisFileConfidentialPage.type, JsValue)] ::
      arbitrary[(DoYouWantToUploadDocumentsPage.type, JsValue)] ::
      arbitrary[(ApplicationContactDetailsPage.type, JsValue)] ::
      arbitrary[(CheckRegisteredDetailsPage.type, JsValue)] ::
      arbitrary[(HasConfidentialInformationPage.type, JsValue)] ::
      arbitrary[(ConfidentialInformationPage.type, JsValue)] ::
      arbitrary[(RequiredInformationPage.type, JsValue)] ::
      arbitrary[(ImportGoodsPage.type, JsValue)] ::
      arbitrary[(HasCommodityCodePage.type, JsValue)] ::
      arbitrary[(CommodityCodePage.type, JsValue)] ::
      arbitrary[(ValuationMethodPage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id      <- nonEmptyString
        draftId <- Gen.chooseNum(0, 999999999)
        data    <- generators match {
                     case Nil => Gen.const(Map[QuestionPage[?], JsValue]())
                     case _   => Gen.mapOf(oneOf(generators))
                   }
      } yield UserAnswers(
        userId = id,
        draftId = DraftId(draftId),
        data = data.foldLeft(Json.obj()) { case (obj, (path, value)) =>
          obj.setObject(path.path, value).get
        }
      )
    }
  }
}
