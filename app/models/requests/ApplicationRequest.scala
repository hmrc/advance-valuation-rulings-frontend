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
import uk.gov.hmrc.auth.core.AffinityGroup

import models.{CheckRegisteredDetails, DraftId, UserAnswers}
import pages._

case class GoodsDetails(
  goodsName: String,
  goodsDescription: String,
  envisagedCommodityCode: Option[String],
  knownLegalProceedings: Option[String],
  confidentialInformation: Option[String]
)

object GoodsDetails {
  implicit val format: OFormat[GoodsDetails] = Json.format[GoodsDetails]

  def apply(userAnswers: UserAnswers): ValidatedNel[QuestionPage[_], GoodsDetails] = {
    val goodsDescription: ValidatedNel[QuestionPage[_], String] =
      userAnswers.validated(DescriptionOfGoodsPage)

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
          goodsName = description,
          goodsDescription = description,
          envisagedCommodityCode = envisagedCommodityCode,
          knownLegalProceedings = knownLegalProceedings,
          confidentialInformation = confidentialInformation
        )
    )
  }
}

final case class TraderDetail(
  eori: String,
//  consentToDisclosureOfPersonalData: Boolean,
  businessName: String,
  addressLine1: String,
  addressLine2: Option[String],
  addressLine3: Option[String],
  postcode: String,
  countryCode: String,
  phoneNumber: Option[String]
)

object TraderDetail {
  implicit val format: OFormat[TraderDetail] = Json.format[TraderDetail]

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, TraderDetail] =
    userAnswers.validatedF[CheckRegisteredDetails, TraderDetail](
      CheckRegisteredDetailsPage,
      (crd: CheckRegisteredDetails) =>
        TraderDetail(
          eori = crd.eori,
//          consentToDisclosureOfPersonalData = crd.consentToDisclosureOfPersonalData,
          businessName = crd.name,
          addressLine1 = crd.streetAndNumber,
          addressLine2 = Some(crd.city),
          addressLine3 = None,
          postcode = crd.postalCode.getOrElse(""),
          countryCode = crd.country,
          phoneNumber = crd.phoneNumber
        )
    )

  def agent(userAnswers: UserAnswers): ValidatedNel[Page, TraderDetail] = {
    val role    = userAnswers.validated(WhatIsYourRoleAsImporterPage)
    val details = userAnswers.validatedF[CheckRegisteredDetails, TraderDetail](
      CheckRegisteredDetailsPage,
      (crd) =>
        TraderDetail(
          eori = crd.eori,
//          consentToDisclosureOfPersonalData = crd.consentToDisclosureOfPersonalData,
          businessName = crd.name,
          addressLine1 = crd.streetAndNumber,
          addressLine2 = Some(crd.city),
          addressLine3 = None,
          postcode = crd.postalCode.getOrElse(""),
          countryCode = crd.country,
          phoneNumber = crd.phoneNumber
        )
    )

    role.andThen(_ => details)
  }
}

case class ApplicationRequest(
  draftId: DraftId,
  trader: TraderDetail,
  agent: Option[TraderDetail],
  contact: ContactDetails,
  requestedMethod: RequestedMethod,
  goodsDetails: GoodsDetails,
  attachments: Seq[AttachmentRequest]
)

object ApplicationRequest {
  private[models] val jsonConfig                   = JsonConfiguration(
    discriminator = "type",
    typeNaming =
      JsonNaming(fullName => fullName.slice(1 + fullName.lastIndexOf("."), fullName.length))
  )
  implicit val format: OFormat[ApplicationRequest] =
    Json.configured(jsonConfig).format[ApplicationRequest]

  def apply(
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): ValidatedNel[Page, ApplicationRequest] = {
    val traderDetail    = TraderDetail(userAnswers)
    val goodsDetails    = GoodsDetails(userAnswers)
    val contact         = ContactDetails(userAnswers, affinityGroup)
    val requestedMethod = RequestedMethod(userAnswers)
    val attachments     = AttachmentRequest(userAnswers)

    (traderDetail, contact, requestedMethod, goodsDetails, attachments).mapN(
      (traderDetail, contact, requestedMethod, goodsDetails, attachments) =>
        ApplicationRequest(
          userAnswers.draftId,
          traderDetail,
          None,
          contact,
          requestedMethod,
          goodsDetails,
          attachments
        )
    )
  }
}
