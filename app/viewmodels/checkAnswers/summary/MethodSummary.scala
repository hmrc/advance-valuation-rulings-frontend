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

package viewmodels.checkAnswers.summary

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import models.UserAnswers
import models.ValuationMethod.{Method1, Method2, Method3, Method4, Method5, Method6}
import pages.ValuationMethodPage
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

case class MethodSummary(rows: SummaryList) extends AnyVal

object MethodSummary {
  def apply(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary     =
    userAnswers.get(ValuationMethodPage) match {
      case Some(Method1) => methodOne(userAnswers)
      case Some(Method2) => methodTwo(userAnswers)
      case Some(Method3) => methodThree(userAnswers)
      case Some(Method4) => methodFour(userAnswers)
      case Some(Method5) => methodFive(userAnswers)
      case Some(Method6) => methodSix(userAnswers)
      case None          => MethodSummary(SummaryListViewModel(Seq.empty)) // Could be none
    }
  def methodOne(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      IsThereASaleInvolvedSummary.row(userAnswers),
      IsSaleBetweenRelatedPartiesSummary.row(userAnswers),
      AreThereRestrictionsOnTheGoodsSummary.row(userAnswers),
      DescribeTheRestrictionsSummary.row(userAnswers),
      IsTheSaleSubjectToConditionsSummary.row(userAnswers),
      DescribeTheConditionsSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  def methodTwo(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyIdenticalGoodsSummary.row(userAnswers),
      HaveYouUsedMethodOneInPastSummary.row(userAnswers),
      ExplainYourGoodsComparingToIdenticalGoodsSummary.row(userAnswers),
      DescribeTheIdenticalGoodsSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  def methodThree(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyTransactionValueOfSimilarGoodsSummary.row(userAnswers),
      HaveYouUsedMethodOneForSimilarGoodsInPastSummary.row(userAnswers),
      WillYouCompareToSimilarGoodsSummary.row(userAnswers),
      ExplainYourGoodsComparingToSimilarGoodsSummary.row(userAnswers),
      DescribeTheSimilarGoodsSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  def methodFour(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      ExplainWhyYouHaveNotSelectedMethodOneToThreeSummary.row(userAnswers),
      ExplainWhyYouChoseMethodFourSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  def methodFive(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {
    println("here")
    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyComputedValueSummary.row(userAnswers),
      ExplainReasonComputedValueSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  def methodSix(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      ExplainWhyYouHaveNotSelectedMethodOneToFiveSummary.row(userAnswers),
      AdaptMethodSummary.row(userAnswers),
      ExplainHowYouWillUseMethodSixSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }
}
