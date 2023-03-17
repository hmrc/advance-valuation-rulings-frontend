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

package models

case class ApplicationViewModel(
  applicant: Applicant,
  requestedMethod: RequestedMethod,
  goodsDetails: GoodsDetails,
  attachments: Seq[UploadedDocument]
)

/** determined via `_type` field, e.g. "_type" = "IndividualApplicant"
  */
sealed trait Applicant
case class IndividualApplicant(
  holder: EORIDetails,
  contact: ContactDetails
) extends Applicant

case class UploadedDocument(
  id: String,
  name: String,
  url: String,
  public: Boolean, // isConfidential
  mimeType: String,
  size: Long
)

case class GoodsDetails(
  goodDescription: String,
  envisagedCommodityCode: Option[String],
  knownLegalProceedings: Option[String],
  confidentialInformation: Option[String]
)

case class EORIDetails(
  eori: String,
  businessName: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  postcode: String,
  country: String
)

case class ContactDetails(
  name: String,
  email: String,
  phone: Option[String]
)

/** determined via `_type` field, e.g. "_type" = "MethodOne" { "_type": "MethodOne",
  * "saleBetweenRelatedParties": "blah blah", ...
  */
sealed trait RequestedMethod

case class MethodOne(
  saleBetweenRelatedParties: Option[String],
  goodsRestrictions: Option[String],
  saleConditions: Option[String]
) extends RequestedMethod

sealed trait IdenticalGoodsExplaination
// again, "_type": "PreviouslyImportedGoods", etc
case class PreviousIdenticalGoods(value: String) extends IdenticalGoodsExplaination
case class OtherUsersIdenticalGoods(value: String) extends IdenticalGoodsExplaination

case class MethodTwo(
  whyNotOtherMethods: String,
  detailedDescription: IdenticalGoodsExplaination
) extends RequestedMethod

sealed trait SimilarGoodsExplaination
// again, "_type": "PreviouslyImportedGoods", etc
case class PreviousSimilarGoods(value: String) extends SimilarGoodsExplaination
case class OtherUsersSimilarGoods(value: String) extends SimilarGoodsExplaination

case class MethodThree(
  whyNotOtherMethods: String,
  detailedDescription: SimilarGoodsExplaination
) extends RequestedMethod

case class MethodFour(
  whyNotOtherMethods: String,
  deductiveMethod: String
) extends RequestedMethod

case class MethodFive(
  whyNotOtherMethods: Boolean,
  computedValue: String
) extends RequestedMethod

case class MethodSix(
  whyNotOtherMethods: String,
  adoptMethod: AdoptMethod,
  valuationDescription: String
) extends RequestedMethod

sealed trait AdoptMethod
object AdoptMethod {
  // likely to serialise as "MethodOne", etc
  case object MethodOne extends AdoptMethod
  case object MethodTwo extends AdoptMethod
  case object MethodThree extends AdoptMethod
  case object MethodFour extends AdoptMethod
  case object MethodFive extends AdoptMethod
  case object Unable extends AdoptMethod
}
