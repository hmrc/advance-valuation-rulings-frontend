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

import cats.data.ValidatedNel
import cats.implicits._

import play.api.libs.json._

import models.{CheckRegisteredDetails, UserAnswers}
import pages._

case class GoodsDetails(
  goodName: String,
  goodDescription: String,
  envisagedCommodityCode: Option[String],
  knownLegalProceedings: Option[String],
  confidentialInformation: Option[String]
)

object GoodsDetails {
  implicit val format: OFormat[GoodsDetails] = Json.format[GoodsDetails]

  def apply(userAnswers: UserAnswers): ValidatedNel[QuestionPage[_], GoodsDetails] = {
    val goodsDescription: ValidatedNel[QuestionPage[_], String] =
      userAnswers.validated(DescriptionOfGoodsPage)

    // val name                   = goodsDescription // there is no name page?
    val envisagedCommodityCode = for {
      hasCode <- userAnswers.get(HasCommodityCodePage)
      code    <- userAnswers.get(CommodityCodePage)
    } yield code

    val knownLegalProceedings = for {
      hasLegalProceedings <- userAnswers.get(HaveTheGoodsBeenSubjectToLegalChallengesPage)
      legalProceedings    <- userAnswers.get(DescribeTheLegalChallengesPage)
    } yield legalProceedings

    val confidentialInformation = for {
      hasConfidentialInformation <- userAnswers.get(HasConfidentialInformationPage)
      confidentialInformation    <- userAnswers.get(ConfidentialInformationPage)
    } yield confidentialInformation

    goodsDescription.map(
      description =>
        GoodsDetails(
          goodName = description,
          goodDescription = description,
          envisagedCommodityCode = envisagedCommodityCode,
          knownLegalProceedings = knownLegalProceedings,
          confidentialInformation = confidentialInformation
        )
    )
  }
}

final case class EORIDetails(
  eori: String,
  businessName: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  postcode: String,
  country: String
)

object EORIDetails {
  implicit val format: OFormat[EORIDetails] = Json.format[EORIDetails]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, EORIDetails] =
    userAnswers.validatedF[CheckRegisteredDetails, EORIDetails](
      CheckRegisteredDetailsPage,
      (crd: CheckRegisteredDetails) =>
        EORIDetails(
          eori = crd.eori,
          businessName = crd.name,
          addressLine1 = crd.streetAndNumber,
          addressLine2 = "",
          addressLine3 = crd.city,
          postcode = crd.postalCode.getOrElse(""),
          country = crd.country
        )
    )
}

case class ApplicationRequest(
  applicationNumber: String,
  eoriDetails: EORIDetails,
  applicant: Applicant,
  requestedMethod: RequestedMethod,
  goodsDetails: GoodsDetails,
  attachments: Seq[Attachment]
)

object ApplicationRequest {
  private[models] val jsonConfig                   = JsonConfiguration(
    discriminator = "_type",
    typeNaming =
      JsonNaming(fullName => fullName.slice(1 + fullName.lastIndexOf("."), fullName.length))
  )
  implicit val format: OFormat[ApplicationRequest] =
    Json.configured(jsonConfig).format[ApplicationRequest]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, ApplicationRequest] = {
    val applicationNumber = userAnswers.applicationNumber
    val eoriDetails       = EORIDetails(userAnswers)
    val goodsDetails      = GoodsDetails(userAnswers)
    val applicant         = Applicant(userAnswers)
    val requestedMethod   = RequestedMethod(userAnswers)
    val attachments       = Attachment(userAnswers)

    (goodsDetails, eoriDetails, applicant, requestedMethod, attachments).mapN(
      (goodsDetails, eoriDetails, applicant, requestedMethod, attachments) =>
        ApplicationRequest(
          applicationNumber,
          eoriDetails,
          applicant,
          requestedMethod,
          goodsDetails,
          attachments
        )
    )
  }
}
