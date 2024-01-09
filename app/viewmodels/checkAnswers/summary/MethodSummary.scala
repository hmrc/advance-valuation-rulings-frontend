/*
 * Copyright 2024 HM Revenue & Customs
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
import pages._
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

case class MethodSummary(rows: SummaryList) extends AnyVal

object MethodSummary {
  def apply(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary =
    userAnswers.get(ValuationMethodPage) match {
      case Some(Method1) => methodOne(userAnswers)
      case Some(Method2) => methodTwo(userAnswers)
      case Some(Method3) => methodThree(userAnswers)
      case Some(Method4) => methodFour(userAnswers)
      case Some(Method5) => methodFive(userAnswers)
      case Some(Method6) => methodSix(userAnswers)
      case None          => MethodSummary(SummaryListViewModel(Seq.empty)) // Could be none
    }

  private def methodOne(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val conditionsRows = userAnswers.get(IsTheSaleSubjectToConditionsPage) match {
      case Some(true)  =>
        Seq(
          IsTheSaleSubjectToConditionsSummary.row(userAnswers),
          DescribeTheConditionsSummary.row(userAnswers)
        ).flatten
      case Some(false) => Seq(IsTheSaleSubjectToConditionsSummary.row(userAnswers)).flatten
      case None        => Seq.empty
    }

    val restrictionsRows = userAnswers.get(AreThereRestrictionsOnTheGoodsPage) match {
      case Some(true)  =>
        Seq(
          AreThereRestrictionsOnTheGoodsSummary.row(userAnswers),
          DescribeTheRestrictionsSummary.row(userAnswers)
        ).flatten
      case Some(false) => Seq(AreThereRestrictionsOnTheGoodsSummary.row(userAnswers)).flatten
      case None        => Seq.empty
    }

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      IsThereASaleInvolvedSummary.row(userAnswers),
      IsSaleBetweenRelatedPartiesSummary.row(userAnswers),
      ExplainHowPartiesAreRelatedSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows ++ restrictionsRows ++ conditionsRows))
  }

  private def methodTwo(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {
    val identicalGoodsRows =
      Seq(
        HaveYouUsedMethodOneInPastSummary.row(userAnswers),
        DescribeTheIdenticalGoodsSummary.row(userAnswers)
      ).flatten

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyIdenticalGoodsSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows ++ identicalGoodsRows))
  }

  private def methodThree(
    userAnswers: UserAnswers
  )(implicit messages: Messages): MethodSummary = {

    val methodRow                = ValuationMethodSummary.row(userAnswers)
    val whyTransactionValue      = WhyTransactionValueOfSimilarGoodsSummary.row(userAnswers)
    val previousMethodOneSummary = HaveYouUsedMethodOneForSimilarGoodsInPastSummary.row(userAnswers)
    val describeSimilarGoods     = DescribeTheSimilarGoodsSummary.row(userAnswers)

    val rows = Seq(
      methodRow,
      whyTransactionValue,
      previousMethodOneSummary,
      describeSimilarGoods
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  private def methodFour(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      ExplainWhyYouHaveNotSelectedMethodOneToThreeSummary.row(userAnswers),
      ExplainWhyYouChoseMethodFourSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  private def methodFive(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {
    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyComputedValueSummary.row(userAnswers),
      ExplainReasonComputedValueSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  private def methodSix(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      ExplainWhyYouHaveNotSelectedMethodOneToFiveSummary.row(userAnswers),
      AdaptMethodSummary.row(userAnswers),
      ExplainHowYouWillUseMethodSixSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }
}
