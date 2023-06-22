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

package pages

import play.api.libs.json._

import models.Index
import pages._
import queries.Modifiable

trait QuestionPage[A] extends Page with Modifiable[A] {
  def path: JsPath = JsPath \ toString
}

trait WithIndexedKeys { page: { def toString(): String } =>
  val key0: String = s"${page.toString()}:${Index(0).position}"
  val key1: String = s"${page.toString()}:${Index(1).position}"
  val key2: String = s"${page.toString()}:${Index(2).position}"
  val key3: String = s"${page.toString()}:${Index(3).position}"
  val key4: String = s"${page.toString()}:${Index(4).position}"
}

object QuestionPage {
  implicit def writes(o: QuestionPage[_]): JsValue =
    JsString(o.toString)

  implicit def reads: Reads[QuestionPage[_]] = {
    case JsString(value) =>

      keysAndPages.get(value) match {
        case Some(page) => JsSuccess(page)
        case None       => JsError(s"Unknown question page key: $value")
      }

    case _               => JsError("Invalid QuestionPage")
  }

  val keysAndPages = Map[String, QuestionPage[_]](
    (AccountHomePage.key, AccountHomePage),
    (AdaptMethodPage.key, AdaptMethodPage),
    (AgentCompanyDetailsPage.key, AgentCompanyDetailsPage),
    (ApplicationContactDetailsPage.key, ApplicationContactDetailsPage),
    (AreThereRestrictionsOnTheGoodsPage.key, AreThereRestrictionsOnTheGoodsPage),
    (BusinessContactDetailsPage.key, BusinessContactDetailsPage),
    (CheckRegisteredDetailsPage.key, CheckRegisteredDetailsPage),
    (CommodityCodePage.key, CommodityCodePage),
    (ConfidentialInformationPage.key, ConfidentialInformationPage),
    (DescribeTheConditionsPage.key, DescribeTheConditionsPage),
    (DescribeTheIdenticalGoodsPage.key, DescribeTheIdenticalGoodsPage),
    (DescribeTheLegalChallengesPage.key, DescribeTheLegalChallengesPage),
    (DescribeTheRestrictionsPage.key, DescribeTheRestrictionsPage),
    (DescribeTheSimilarGoodsPage.key, DescribeTheSimilarGoodsPage),
    (DescriptionOfGoodsPage.key, DescriptionOfGoodsPage),
    (DoYouWantToUploadDocumentsPage.key, DoYouWantToUploadDocumentsPage),
    (ExplainHowPartiesAreRelatedPage.key, ExplainHowPartiesAreRelatedPage),
    (ExplainHowYouWillUseMethodSixPage.key, ExplainHowYouWillUseMethodSixPage),
    (ExplainReasonComputedValuePage.key, ExplainReasonComputedValuePage),
    (ExplainWhyYouChoseMethodFourPage.key, ExplainWhyYouChoseMethodFourPage),
    (
      ExplainWhyYouHaveNotSelectedMethodOneToThreePage.key,
      ExplainWhyYouHaveNotSelectedMethodOneToThreePage
    ),
    (
      ExplainWhyYouHaveNotSelectedMethodOneToFivePage.key,
      ExplainWhyYouHaveNotSelectedMethodOneToFivePage
    ),
    (HasCommodityCodePage.key, HasCommodityCodePage),
    (HasConfidentialInformationPage.key, HasConfidentialInformationPage),
    (
      HaveTheGoodsBeenSubjectToLegalChallengesPage.key,
      HaveTheGoodsBeenSubjectToLegalChallengesPage
    ),
    (
      HaveYouUsedMethodOneForSimilarGoodsInPastPage.key,
      HaveYouUsedMethodOneForSimilarGoodsInPastPage
    ),
    (HaveYouUsedMethodOneInPastPage.key, HaveYouUsedMethodOneInPastPage),
    (ImportGoodsPage.key, ImportGoodsPage),
    (IsSaleBetweenRelatedPartiesPage.key, IsSaleBetweenRelatedPartiesPage),
    (IsThereASaleInvolvedPage.key, IsThereASaleInvolvedPage),
    (IsTheSaleSubjectToConditionsPage.key, IsTheSaleSubjectToConditionsPage),
    (RequiredInformationPage.key, RequiredInformationPage),
    (UploadAnotherSupportingDocumentPage.key, UploadAnotherSupportingDocumentPage),
    (ValuationMethodPage.key, ValuationMethodPage),
    (WhatIsYourRoleAsImporterPage.key, WhatIsYourRoleAsImporterPage),
    (WhoAreYouAgentPage.key, WhoAreYouAgentPage),
    (WhyComputedValuePage.key, WhyComputedValuePage),
    (WhyIdenticalGoodsPage.key, WhyIdenticalGoodsPage),
    (WhyTransactionValueOfSimilarGoodsPage.key, WhyTransactionValueOfSimilarGoodsPage),
    // Special cases
    (IsThisFileConfidentialPage.key0, IsThisFileConfidentialPage(Index(0))),
    (IsThisFileConfidentialPage.key1, IsThisFileConfidentialPage(Index(1))),
    (IsThisFileConfidentialPage.key2, IsThisFileConfidentialPage(Index(2))),
    (IsThisFileConfidentialPage.key3, IsThisFileConfidentialPage(Index(3))),
    (IsThisFileConfidentialPage.key4, IsThisFileConfidentialPage(Index(4))),
    (UploadSupportingDocumentPage.key0, UploadSupportingDocumentPage(Index(0))),
    (UploadSupportingDocumentPage.key1, UploadSupportingDocumentPage(Index(1))),
    (UploadSupportingDocumentPage.key2, UploadSupportingDocumentPage(Index(2))),
    (UploadSupportingDocumentPage.key3, UploadSupportingDocumentPage(Index(3))),
    (UploadSupportingDocumentPage.key4, UploadSupportingDocumentPage(Index(4))),
    (RemoveSupportingDocumentPage.key0, RemoveSupportingDocumentPage(Index(0))),
    (RemoveSupportingDocumentPage.key1, RemoveSupportingDocumentPage(Index(1))),
    (RemoveSupportingDocumentPage.key2, RemoveSupportingDocumentPage(Index(2))),
    (RemoveSupportingDocumentPage.key3, RemoveSupportingDocumentPage(Index(3))),
    (RemoveSupportingDocumentPage.key4, RemoveSupportingDocumentPage(Index(4)))
  )
}
