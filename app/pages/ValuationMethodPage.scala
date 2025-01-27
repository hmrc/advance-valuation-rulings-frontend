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

package pages

import models.{UserAnswers, ValuationMethod}
import play.api.libs.json.JsPath

import scala.util.Try

case object ValuationMethodPage extends QuestionPage[ValuationMethod] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "valuationMethod"

  private def removeMethod1(ua: UserAnswers): Try[UserAnswers] =
    for {
      ua <- ua.remove(IsThereASaleInvolvedPage)
      ua <- ua.remove(IsSaleBetweenRelatedPartiesPage)
      ua <- ua.remove(ExplainHowPartiesAreRelatedPage)
      ua <- ua.remove(AreThereRestrictionsOnTheGoodsPage)
      ua <- ua.remove(DescribeTheRestrictionsPage)
      ua <- ua.remove(IsTheSaleSubjectToConditionsPage)
      ua <- ua.remove(DescribeTheConditionsPage)
    } yield ua

  private def removeMethod2(ua: UserAnswers): Try[UserAnswers] =
    for {
      ua <- ua.remove(WhyIdenticalGoodsPage)
      ua <- ua.remove(HaveYouUsedMethodOneInPastPage)
      ua <- ua.remove(DescribeTheIdenticalGoodsPage)
    } yield ua

  private def removeMethod3(ua: UserAnswers): Try[UserAnswers] =
    for {
      ua <- ua.remove(WhyTransactionValueOfSimilarGoodsPage)
      ua <- ua.remove(HaveYouUsedMethodOneForSimilarGoodsInPastPage)
      ua <- ua.remove(DescribeTheSimilarGoodsPage)
    } yield ua

  private def removeMethod4(ua: UserAnswers): Try[UserAnswers] =
    for {
      ua <- ua.remove(ExplainWhyYouHaveNotSelectedMethodOneToThreePage)
      ua <- ua.remove(ExplainWhyYouChoseMethodFourPage)
    } yield ua

  private def removeMethod5(ua: UserAnswers): Try[UserAnswers] =
    for {
      ua <- ua.remove(WhyComputedValuePage)
      ua <- ua.remove(ExplainReasonComputedValuePage)
    } yield ua

  private def removeMethod6(ua: UserAnswers): Try[UserAnswers] =
    for {
      ua <- ua.remove(ExplainWhyYouHaveNotSelectedMethodOneToFivePage)
      ua <- ua.remove(AdaptMethodPage)
      ua <- ua.remove(ExplainHowYouWillUseMethodSixPage)
    } yield ua

  override def cleanup(value: Option[ValuationMethod], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case None =>
        for {
          ua <- removeMethod1(userAnswers)
          ua <- removeMethod2(ua)
          ua <- removeMethod3(ua)
          ua <- removeMethod4(ua)
          ua <- removeMethod5(ua)
          ua <- removeMethod6(ua)
        } yield ua

      case Some(ValuationMethod.Method1) =>
        for {
          ua <- removeMethod2(userAnswers)
          ua <- removeMethod3(ua)
          ua <- removeMethod4(ua)
          ua <- removeMethod5(ua)
          ua <- removeMethod6(ua)
        } yield ua

      case Some(ValuationMethod.Method2) =>
        for {
          ua <- removeMethod1(userAnswers)
          ua <- removeMethod3(ua)
          ua <- removeMethod4(ua)
          ua <- removeMethod5(ua)
          ua <- removeMethod6(ua)
        } yield ua

      case Some(ValuationMethod.Method3) =>
        for {
          ua <- removeMethod1(userAnswers)
          ua <- removeMethod2(ua)
          ua <- removeMethod4(ua)
          ua <- removeMethod5(ua)
          ua <- removeMethod6(ua)
        } yield ua

      case Some(ValuationMethod.Method4) =>
        for {
          ua <- removeMethod1(userAnswers)
          ua <- removeMethod2(ua)
          ua <- removeMethod3(ua)
          ua <- removeMethod5(ua)
          ua <- removeMethod6(ua)
        } yield ua

      case Some(ValuationMethod.Method5) =>
        for {
          ua <- removeMethod1(userAnswers)
          ua <- removeMethod2(ua)
          ua <- removeMethod3(ua)
          ua <- removeMethod4(ua)
          ua <- removeMethod6(ua)
        } yield ua

      case Some(ValuationMethod.Method6) =>
        for {
          ua <- removeMethod1(userAnswers)
          ua <- removeMethod2(ua)
          ua <- removeMethod3(ua)
          ua <- removeMethod4(ua)
          ua <- removeMethod5(ua)
        } yield ua

    }
}
