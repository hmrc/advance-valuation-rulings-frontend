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

import play.api.libs.json._

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

sealed trait RequestedMethod

case class MethodOne(
  saleBetweenRelatedParties: Option[String],
  goodsRestrictions: Option[String],
  saleConditions: Option[String]
) extends RequestedMethod
object MethodOne {
  implicit val format: OFormat[MethodOne] = Json.format[MethodOne]
}

sealed trait IdenticalGoodsExplaination extends Any
case class PreviousIdenticalGoods(val value: String) extends AnyVal with IdenticalGoodsExplaination
object PreviousIdenticalGoods {
  implicit val format: Format[PreviousIdenticalGoods] = Json.valueFormat[PreviousIdenticalGoods]
}
case class OtherUsersIdenticalGoods(val value: String)
    extends AnyVal
    with IdenticalGoodsExplaination
object OtherUsersIdenticalGoods {
  implicit val format = Json.valueFormat[OtherUsersIdenticalGoods]
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
}

sealed trait SimilarGoodsExplaination extends Any
case class PreviousSimilarGoods(val value: String) extends AnyVal with SimilarGoodsExplaination
case class OtherUsersSimilarGoods(val value: String) extends AnyVal with SimilarGoodsExplaination

object PreviousSimilarGoods {
  implicit val format: Format[PreviousSimilarGoods] = Json.valueFormat[PreviousSimilarGoods]
}
object OtherUsersSimilarGoods {
  implicit val format: Format[OtherUsersSimilarGoods] = Json.valueFormat[OtherUsersSimilarGoods]
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
}

case class MethodFour(
  whyNotOtherMethods: String,
  deductiveMethod: String
) extends RequestedMethod
object MethodFour {
  implicit val format: OFormat[MethodFour] = Json.format[MethodFour]
}

case class MethodFive(
  whyNotOtherMethods: String,
  computedValue: String
) extends RequestedMethod
object MethodFive {
  implicit val format: OFormat[MethodFive] = Json.format[MethodFive]
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
}

case class MethodSix(
  whyNotOtherMethods: String,
  adoptMethod: AdaptedMethod,
  valuationDescription: String
) extends RequestedMethod
object MethodSix {
  implicit val format: OFormat[MethodSix] = Json.format[MethodSix]
}

object RequestedMethod {
  private[models] val jsonConfig                   = JsonConfiguration(
    discriminator = "_type",
    typeNaming =
      JsonNaming(fullName => fullName.slice(1 + fullName.lastIndexOf("."), fullName.length))
  )
  implicit val format: OFormat[RequestedMethod] =
    Json.configured(jsonConfig).format[RequestedMethod]
}
