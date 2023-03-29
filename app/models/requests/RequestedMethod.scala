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

package models.requests

import cats.data.{NonEmptyList, ValidatedNel}
import cats.implicits._

import play.api.libs.json.{Format, Json, JsonConfiguration, JsonNaming, OFormat}

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import models.{AdaptMethod, UserAnswers, ValuationMethod}
import pages._

sealed trait RequestedMethod

case class MethodOne(
  saleBetweenRelatedParties: Option[String],
  goodsRestrictions: Option[String],
  saleConditions: Option[String]
) extends RequestedMethod
object MethodOne {
  implicit val format: OFormat[MethodOne] = Json.format[MethodOne]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, MethodOne] = {
    val saleInvolved = userAnswers
      .validated(IsThereASaleInvolvedPage)
      .ensure(NonEmptyList.one(IsThereASaleInvolvedPage))(_ == true)

    val betweenParties: ValidatedNel[Page, Option[String]] = userAnswers
      .validated(IsSaleBetweenRelatedPartiesPage)
      .andThen(
        answer =>
          if (answer) userAnswers.validated(ExplainHowPartiesAreRelatedPage).map(_.some)
          else None.validNel
      )

    val restrictions = userAnswers
      .validated(AreThereRestrictionsOnTheGoodsPage)
      .andThen(
        answer =>
          if (answer) userAnswers.validated(DescribeTheRestrictionsPage).map(_.some)
          else None.validNel
      )

    val subjectToConditions = userAnswers
      .validated(IsTheSaleSubjectToConditionsPage)
      .andThen(
        answer =>
          if (answer) userAnswers.validated(DescribeTheConditionsPage).map(_.some)
          else None.validNel
      )

    saleInvolved.andThen(
      _ => (betweenParties, restrictions, subjectToConditions).mapN(MethodOne.apply)
    )
  }
}

sealed trait IdenticalGoodsExplaination extends Any
case class PreviousIdenticalGoods(val value: String) extends AnyVal with IdenticalGoodsExplaination
object PreviousIdenticalGoods {
  implicit val format: Format[PreviousIdenticalGoods] = Json.valueFormat[PreviousIdenticalGoods]
}

object IdenticalGoodsExplaination {
  import ApplicationRequest.jsonConfig
  implicit val format: Format[IdenticalGoodsExplaination] =
    Json.configured(jsonConfig).format[IdenticalGoodsExplaination]
}

case class MethodTwo(
  whyNotOtherMethods: String,
  detailedDescription: IdenticalGoodsExplaination
) extends RequestedMethod
object MethodTwo {
  implicit val format: OFormat[MethodTwo] = Json.format[MethodTwo]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, MethodTwo] = {
    val whyTransactionValue       = userAnswers.validated(WhyIdenticalGoodsPage)
    val usedMethod                = userAnswers
      .validated(HaveYouUsedMethodOneInPastPage)
      .ensure(NonEmptyList.one(HaveYouUsedMethodOneInPastPage))(_ == true)
    val describeTheIdenticalGoods = userAnswers.validated(DescribeTheIdenticalGoodsPage)

    (whyTransactionValue, usedMethod, describeTheIdenticalGoods)
      .mapN(
        (why, _, describe) =>
          MethodTwo(
            why,
            PreviousIdenticalGoods(describe)
          )
      )
  }
}

sealed trait SimilarGoodsExplaination extends Any
case class PreviousSimilarGoods(val value: String) extends AnyVal with SimilarGoodsExplaination
object PreviousSimilarGoods {
  implicit val format: Format[PreviousSimilarGoods] = Json.valueFormat[PreviousSimilarGoods]
}

object SimilarGoodsExplaination {
  import ApplicationRequest.jsonConfig
  implicit val format: OFormat[SimilarGoodsExplaination] =
    Json.configured(jsonConfig).format[SimilarGoodsExplaination]
}

case class MethodThree(
  whyNotOtherMethods: String,
  detailedDescription: SimilarGoodsExplaination
) extends RequestedMethod
object MethodThree {
  implicit val format: Format[MethodThree] =
    Json.format[MethodThree]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, MethodThree] = {
    val usedMethod              = userAnswers
      .validated(HaveYouUsedMethodOneForSimilarGoodsInPastPage)
      .ensure(NonEmptyList.one(HaveYouUsedMethodOneForSimilarGoodsInPastPage))(_ == true)
    val whyTransactionValue     = userAnswers.validated(WhyTransactionValueOfSimilarGoodsPage)
    val describeTheSimilarGoods = userAnswers.validated(DescribeTheSimilarGoodsPage)

    (whyTransactionValue, usedMethod, describeTheSimilarGoods)
      .mapN((why, _, describe) => MethodThree(why, PreviousSimilarGoods(describe)))
  }
}

case class MethodFour(
  whyNotOtherMethods: String,
  deductiveMethod: String
) extends RequestedMethod
object MethodFour {
  implicit val format: OFormat[MethodFour] = Json.format[MethodFour]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, MethodFour] = {
    val whyNotOtherMethods = userAnswers.validated(ExplainWhyYouHaveNotSelectedMethodOneToThreePage)
    val deductiveMethod    = userAnswers.validated(ExplainWhyYouChoseMethodFourPage)

    (whyNotOtherMethods, deductiveMethod).mapN(MethodFour.apply)
  }
}

case class MethodFive(
  whyNotOtherMethods: String,
  computedValue: String
) extends RequestedMethod
object MethodFive {
  implicit val format: OFormat[MethodFive] = Json.format[MethodFive]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, MethodFive] = {
    val whyNotOtherMethods = userAnswers.validated(WhyComputedValuePage)
    val computedValue      = userAnswers.validated(ExplainReasonComputedValuePage)

    (whyNotOtherMethods, computedValue).mapN(MethodFive.apply)
  }
}

sealed abstract class AdaptedMethod(override val entryName: String) extends EnumEntry

object AdaptedMethod extends Enum[AdaptedMethod] with PlayJsonEnum[AdaptedMethod] {
  val values: IndexedSeq[AdaptedMethod] = findValues

  case object MethodOne extends AdaptedMethod("MethodOne")
  case object MethodTwo extends AdaptedMethod("MethodTwo")
  case object MethodThree extends AdaptedMethod("MethodThree")
  case object MethodFour extends AdaptedMethod("MethodFour")
  case object MethodFive extends AdaptedMethod("MethodFive")
  case object Unable extends AdaptedMethod("Unable")

  def apply(adaptMethod: AdaptMethod): AdaptedMethod = adaptMethod match {
    case AdaptMethod.Method1       => MethodOne
    case AdaptMethod.Method2       => MethodTwo
    case AdaptMethod.Method3       => MethodThree
    case AdaptMethod.Method4       => MethodFour
    case AdaptMethod.Method5       => MethodFive
    case AdaptMethod.NoOtherMethod => Unable
  }
}

case class MethodSix(
  whyNotOtherMethods: String,
  adaptMethod: AdaptedMethod,
  valuationDescription: String
) extends RequestedMethod
object MethodSix {
  implicit val format: OFormat[MethodSix] = Json.format[MethodSix]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, MethodSix] = {
    val whyNotOtherMethods   =
      userAnswers.validated(ExplainWhyYouHaveNotSelectedMethodOneToFivePage)
    val adaptMethod          = userAnswers.validated(AdaptMethodPage).map(AdaptedMethod.apply)
    val valuationDescription =
      userAnswers.validated(ExplainHowYouWillUseMethodSixPage)

    (whyNotOtherMethods, adaptMethod, valuationDescription).mapN(MethodSix.apply)
  }
}

object RequestedMethod {
  private[models] val jsonConfig                = JsonConfiguration(
    discriminator = "_type",
    typeNaming =
      JsonNaming(fullName => fullName.slice(1 + fullName.lastIndexOf("."), fullName.length))
  )
  implicit val format: OFormat[RequestedMethod] =
    Json.configured(jsonConfig).format[RequestedMethod]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, RequestedMethod] = {
    val method = userAnswers.validated(ValuationMethodPage)

    method.andThen {
      case ValuationMethod.Method1 => MethodOne(userAnswers)
      case ValuationMethod.Method2 => MethodTwo(userAnswers)
      case ValuationMethod.Method3 => MethodThree(userAnswers)
      case ValuationMethod.Method4 => MethodFour(userAnswers)
      case ValuationMethod.Method5 => MethodFive(userAnswers)
      case ValuationMethod.Method6 => MethodSix(userAnswers)
    }
  }
}
