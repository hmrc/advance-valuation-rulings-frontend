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

package viewmodels.application

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import models.AdaptMethod
import models.ValuationMethod._
import models.requests._
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object RequestedMethodSummary {

  def rows(requestedMethod: RequestedMethod)(implicit messages: Messages): Seq[SummaryListRow] =
    requestedMethod match {
      case method: MethodOne   => method1Rows(method)
      case method: MethodTwo   => method2Rows(method)
      case method: MethodThree => method3Rows(method)
      case method: MethodFour  => method4Rows(method)
      case method: MethodFive  => method5Rows(method)
      case method: MethodSix   => method6Rows(method)
    }

  private def method1Rows(method: MethodOne)(implicit messages: Messages): Seq[SummaryListRow] = {

    val methodRow = Some(
      SummaryListRowViewModel(
        key = "valuationMethod.checkYourAnswersLabel",
        value = ValueViewModel(messages(s"valuationMethod.${Method1.toString}"))
      )
    )

    val saleInvolvedRow = Some(
      SummaryListRowViewModel(
        key = "isThereASaleInvolved.checkYourAnswersLabel",
        value = ValueViewModel(messages("site.yes"))
      )
    )

    val relatedPartiesRow = method.saleBetweenRelatedParties.map {
      info =>
        SummaryListRowViewModel(
          key = "explainHowPartiesAreRelated.checkYourAnswersLabel",
          value = ValueViewModel(info)
        )
    }

    val conditionsRow = method.saleConditions.map {
      conditions =>
        SummaryListRowViewModel(
          key = "describeTheConditions.checkYourAnswersLabel",
          value = ValueViewModel(conditions)
        )
    }

    val restrictionsRow = method.goodsRestrictions.map {
      restrictions =>
        SummaryListRowViewModel(
          key = "describeTheRestrictions.checkYourAnswersLabel",
          value = ValueViewModel(restrictions)
        )
    }

    Seq(
      methodRow,
      saleInvolvedRow,
      relatedPartiesRow,
      conditionsRow,
      restrictionsRow
    ).flatten
  }

  private def method2Rows(method: MethodTwo)(implicit messages: Messages): Seq[SummaryListRow] = {

    val methodRow = SummaryListRowViewModel(
      key = "valuationMethod.checkYourAnswersLabel",
      value = ValueViewModel(messages(s"valuationMethod.${Method2.toString}"))
    )

    val whyIdenticalGoodsRow = SummaryListRowViewModel(
      key = "whyIdenticalGoods.checkYourAnswersLabel",
      value = ValueViewModel(method.whyNotOtherMethods)
    )

    val usedMethodOneRow = SummaryListRowViewModel(
      key = "haveYouUsedMethodOneInPast.checkYourAnswersLabel",
      value = ValueViewModel(messages("site.yes"))
    )

    val identicalGoodsRow = SummaryListRowViewModel(
      key = "describeTheIdenticalGoods.checkYourAnswersLabel",
      value = ValueViewModel(method.previousIdenticalGoods.value)
    )

    Seq(
      methodRow,
      whyIdenticalGoodsRow,
      usedMethodOneRow,
      identicalGoodsRow
    )
  }

  private def method3Rows(method: MethodThree)(implicit messages: Messages): Seq[SummaryListRow] = {

    val methodRow = SummaryListRowViewModel(
      key = "valuationMethod.checkYourAnswersLabel",
      value = ValueViewModel(messages(s"valuationMethod.${Method3.toString}"))
    )

    val whySimilarGoods = SummaryListRowViewModel(
      key = "whyTransactionValueOfSimilarGoods.checkYourAnswersLabel",
      value = ValueViewModel(method.whyNotOtherMethods)
    )

    val usedMethodOneRow = SummaryListRowViewModel(
      key = "haveYouUsedMethodOneForSimilarGoodsInPast.checkYourAnswersLabel",
      value = ValueViewModel(messages("site.yes"))
    )

    val similarGoodsRow = SummaryListRowViewModel(
      key = "describeTheSimilarGoods.checkYourAnswersLabel",
      value = ValueViewModel(method.previousSimilarGoods.value)
    )

    Seq(
      methodRow,
      whySimilarGoods,
      usedMethodOneRow,
      similarGoodsRow
    )
  }

  private def method4Rows(method: MethodFour)(implicit messages: Messages): Seq[SummaryListRow] = {

    val methodRow = SummaryListRowViewModel(
      key = "valuationMethod.checkYourAnswersLabel",
      value = ValueViewModel(messages(s"valuationMethod.${Method4.toString}"))
    )

    val whyNotOthersRow = SummaryListRowViewModel(
      key = "explainWhyYouHaveNotSelectedMethodOneToThree.checkYourAnswersLabel",
      value = ValueViewModel(method.whyNotOtherMethods)
    )

    val whyMethod4Row = SummaryListRowViewModel(
      key = "explainWhyYouChoseMethodFour.checkYourAnswersLabel",
      value = ValueViewModel(method.deductiveMethod)
    )

    Seq(
      methodRow,
      whyNotOthersRow,
      whyMethod4Row
    )
  }

  private def method5Rows(method: MethodFive)(implicit messages: Messages): Seq[SummaryListRow] = {

    val methodRow = SummaryListRowViewModel(
      key = "valuationMethod.checkYourAnswersLabel",
      value = ValueViewModel(messages(s"valuationMethod.${Method5.toString}"))
    )

    val whyNotOthersRow = SummaryListRowViewModel(
      key = "whyComputedValue.checkYourAnswersLabel",
      value = ValueViewModel(method.whyNotOtherMethods)
    )

    val computedValueRow = SummaryListRowViewModel(
      key = "explainReasonComputedValue.checkYourAnswersLabel",
      value = ValueViewModel(method.computedValue)
    )

    Seq(
      methodRow,
      whyNotOthersRow,
      computedValueRow
    )
  }

  private def method6Rows(method: MethodSix)(implicit messages: Messages): Seq[SummaryListRow] = {

    val adaptMethod = method.adaptedMethod match {
      case AdaptedMethod.MethodOne   => AdaptMethod.Method1
      case AdaptedMethod.MethodTwo   => AdaptMethod.Method2
      case AdaptedMethod.MethodThree => AdaptMethod.Method3
      case AdaptedMethod.MethodFour  => AdaptMethod.Method4
      case AdaptedMethod.MethodFive  => AdaptMethod.Method5
      case AdaptedMethod.Unable      => AdaptMethod.NoOtherMethod
    }

    val methodRow = SummaryListRowViewModel(
      key = "valuationMethod.checkYourAnswersLabel",
      value = ValueViewModel(messages(s"valuationMethod.${Method6.toString}"))
    )

    val whyNotOthersRow = SummaryListRowViewModel(
      key = "explainWhyYouHaveNotSelectedMethodOneToFive.checkYourAnswersLabel",
      value = ValueViewModel(method.whyNotOtherMethods)
    )

    val adaptedMethodRow = SummaryListRowViewModel(
      key = "adaptMethod.checkYourAnswersLabel",
      value = ValueViewModel(messages(s"adaptMethod.$adaptMethod"))
    )

    val computedValueRow = SummaryListRowViewModel(
      key = "explainHowYouWillUseMethodSix.checkYourAnswersLabel",
      value = ValueViewModel(method.valuationDescription)
    )

    Seq(
      methodRow,
      whyNotOthersRow,
      adaptedMethodRow,
      computedValueRow
    )
  }
}
