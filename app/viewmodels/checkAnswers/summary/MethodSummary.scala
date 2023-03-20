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

import models.{AdaptMethod, UserAnswers, ValuationMethod}
import models.ValuationMethod.{Method1, Method2, Method3, Method4, Method5, Method6}
import models.requests._
import pages._
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

case class MethodSummary(rows: SummaryList) extends AnyVal {
  def removeActions(): MethodSummary =
    MethodSummary(SummaryListViewModel(rows.rows.map(_.copy(actions = None))))
}

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

  protected def methodOne(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

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

  protected def methodOne(method: MethodOne)(implicit messages: Messages): MethodSummary = {
    val valuationMethod = toValuationMethod(method)

    val conditionsRows = method.goodsRestrictions match {
      case Some(answer) =>
        Seq(
          IsTheSaleSubjectToConditionsSummary.row(true),
          DescribeTheConditionsSummary.row(answer)
        )
      case None         => Seq(IsTheSaleSubjectToConditionsSummary.row(false))
    }

    val restrictionsRows = method.goodsRestrictions match {
      case Some(answer) =>
        Seq(
          AreThereRestrictionsOnTheGoodsSummary.row(true),
          DescribeTheRestrictionsSummary.row(answer)
        )
      case None         => Seq(AreThereRestrictionsOnTheGoodsSummary.row(false))
    }

    val rows = Seq(
      ValuationMethodSummary.row(valuationMethod),
      IsThereASaleInvolvedSummary.row(true),
      IsSaleBetweenRelatedPartiesSummary.row(method.saleBetweenRelatedParties.isDefined),
      ExplainHowPartiesAreRelatedSummary.row(method.saleBetweenRelatedParties.getOrElse(""))
    )

    MethodSummary(SummaryListViewModel(rows ++ restrictionsRows ++ conditionsRows))
  }

  protected def methodTwo(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {
    val identicalGoodsRows = userAnswers.get(HaveYouUsedMethodOneInPastPage) match {
      case Some(true)  =>
        Seq(
          HaveYouUsedMethodOneInPastSummary.row(userAnswers),
          DescribeTheIdenticalGoodsSummary.row(userAnswers)
        ).flatten
      case Some(false) =>
        Seq(
          HaveYouUsedMethodOneInPastSummary.row(userAnswers)
        ).flatten
      case None        => Seq.empty
    }

    val compareGoodsRows = userAnswers.get(WillYouCompareGoodsToIdenticalGoodsPage) match {
      case Some(true)  =>
        Seq(
          WillYouCompareGoodsToIdenticalGoodsSummary.row(userAnswers),
          ExplainYourGoodsComparingToIdenticalGoodsSummary.row(userAnswers)
        ).flatten
      case Some(false) =>
        Seq(
          HaveYouUsedMethodOneInPastSummary.row(userAnswers)
        ).flatten
      case None        => Seq.empty
    }

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyIdenticalGoodsSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows ++ identicalGoodsRows ++ compareGoodsRows))
  }

  protected def methodTwo(method: MethodTwo)(implicit messages: Messages): MethodSummary = {
    val valuationMethod = toValuationMethod(method)

    val rows = Seq(
      ValuationMethodSummary.row(valuationMethod),
      WhyIdenticalGoodsSummary.row(method.whyNotOtherMethods)
    )

    val additionalRows = method.detailedDescription match {
      case PreviousIdenticalGoods(answer)   =>
        Seq(
          HaveYouUsedMethodOneInPastSummary.row(true),
          DescribeTheIdenticalGoodsSummary.row(answer)
        )
      case OtherUsersIdenticalGoods(answer) =>
        Seq(
          HaveYouUsedMethodOneInPastSummary.row(false),
          WillYouCompareGoodsToIdenticalGoodsSummary.row(true),
          ExplainYourGoodsComparingToIdenticalGoodsSummary.row(answer)
        )
    }

    MethodSummary(SummaryListViewModel(rows ++ additionalRows))
  }

  protected def methodThree(
    userAnswers: UserAnswers
  )(implicit messages: Messages): MethodSummary = {

    val methodRow = ValuationMethodSummary.row(userAnswers)

    val usedMethod = userAnswers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage)

    val rows = usedMethod match {
      case Some(true) =>
        Seq(
          methodRow,
          WhyTransactionValueOfSimilarGoodsSummary.row(userAnswers),
          HaveYouUsedMethodOneForSimilarGoodsInPastSummary.row(userAnswers),
          DescribeTheSimilarGoodsSummary.row(userAnswers)
        ).flatten
      case _          =>
        Seq(
          ValuationMethodSummary.row(userAnswers),
          WhyTransactionValueOfSimilarGoodsSummary.row(userAnswers),
          HaveYouUsedMethodOneForSimilarGoodsInPastSummary.row(userAnswers),
          WillYouCompareToSimilarGoodsSummary.row(userAnswers),
          ExplainYourGoodsComparingToSimilarGoodsSummary.row(userAnswers),
          DescribeTheSimilarGoodsSummary.row(userAnswers)
        ).flatten
    }

    MethodSummary(SummaryListViewModel(rows))
  }

  protected def methodThree(
    method: MethodThree
  )(implicit messages: Messages): MethodSummary = {

    val valuationMethod = toValuationMethod(method)
    val methodRow       = ValuationMethodSummary.row(valuationMethod)

    val rows = method.detailedDescription match {
      case PreviousSimilarGoods(answer)   =>
        Seq(
          methodRow,
          WhyTransactionValueOfSimilarGoodsSummary.row(method.whyNotOtherMethods),
          HaveYouUsedMethodOneForSimilarGoodsInPastSummary.row(true),
          DescribeTheSimilarGoodsSummary.row(answer)
        )
      case OtherUsersSimilarGoods(answer) =>
        Seq(
          methodRow,
          WhyTransactionValueOfSimilarGoodsSummary.row(method.whyNotOtherMethods),
          HaveYouUsedMethodOneForSimilarGoodsInPastSummary.row(false),
          WillYouCompareToSimilarGoodsSummary.row(true),
          ExplainYourGoodsComparingToSimilarGoodsSummary.row(answer),
          DescribeTheSimilarGoodsSummary.row(answer)
        )
    }

    MethodSummary(SummaryListViewModel(rows))
  }

  protected def methodFour(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      ExplainWhyYouHaveNotSelectedMethodOneToThreeSummary.row(userAnswers),
      ExplainWhyYouChoseMethodFourSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  protected def methodFour(method: MethodFour)(implicit messages: Messages): MethodSummary = {
    val valuationMethod = toValuationMethod(method)
    val rows            = Seq(
      ValuationMethodSummary.row(valuationMethod),
      ExplainWhyYouHaveNotSelectedMethodOneToThreeSummary.row(method.whyNotOtherMethods),
      ExplainWhyYouChoseMethodFourSummary.row(method.deductiveMethod)
    )

    MethodSummary(SummaryListViewModel(rows))
  }

  protected def methodFive(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {
    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      WhyComputedValueSummary.row(userAnswers),
      ExplainReasonComputedValueSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  protected def methodFive(method: MethodFive)(implicit messages: Messages): MethodSummary = {
    val valuationMethod = toValuationMethod(method)
    val rows            = Seq(
      ValuationMethodSummary.row(valuationMethod),
      WhyComputedValueSummary.row(method.whyNotOtherMethods),
      ExplainReasonComputedValueSummary.row(method.computedValue)
    )

    MethodSummary(SummaryListViewModel(rows))
  }

  protected def methodSix(userAnswers: UserAnswers)(implicit messages: Messages): MethodSummary = {

    val rows = Seq(
      ValuationMethodSummary.row(userAnswers),
      ExplainWhyYouHaveNotSelectedMethodOneToFiveSummary.row(userAnswers),
      AdaptMethodSummary.row(userAnswers),
      ExplainHowYouWillUseMethodSixSummary.row(userAnswers)
    ).flatten

    MethodSummary(SummaryListViewModel(rows))
  }

  private def toValuationMethod(method: RequestedMethod): ValuationMethod = method match {
    case _: MethodOne   => Method1
    case _: MethodTwo   => Method2
    case _: MethodThree => Method3
    case _: MethodFour  => Method4
    case _: MethodFive  => Method5
    case _: MethodSix   => Method6
  }

  protected def methodSix(method: MethodSix)(implicit messages: Messages): MethodSummary = {
    val adaptedMethod = method.adoptMethod match {
      case AdaptedMethod.MethodOne   => AdaptMethod.Method1
      case AdaptedMethod.MethodTwo   => AdaptMethod.Method2
      case AdaptedMethod.MethodThree => AdaptMethod.Method3
      case AdaptedMethod.MethodFive  => AdaptMethod.Method5
      case AdaptedMethod.MethodFour  => AdaptMethod.Method4
      case AdaptedMethod.Unable      => AdaptMethod.NoOtherMethod
    }

    val valuationMethod = toValuationMethod(method)

    val rows = Seq(
      ValuationMethodSummary.row(valuationMethod),
      ExplainWhyYouHaveNotSelectedMethodOneToFiveSummary.row(method.whyNotOtherMethods),
      AdaptMethodSummary.row(adaptedMethod),
      ExplainHowYouWillUseMethodSixSummary.row(method.valuationDescription)
    )

    MethodSummary(SummaryListViewModel(rows))
  }

  def apply(request: ApplicationRequest)(implicit messages: Messages): MethodSummary =
    request.requestedMethod match {
      case method: MethodOne   => methodOne(method)
      case method: MethodTwo   => methodTwo(method)
      case method: MethodThree => methodThree(method)
      case method: MethodFour  => methodFour(method)
      case method: MethodFive  => methodFive(method)
      case method: MethodSix   => methodSix(method)
    }
}
