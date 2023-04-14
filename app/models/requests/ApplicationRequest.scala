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

import cats.data.{Validated, ValidatedNel}
import cats.implicits._

import play.api.libs.json._
import uk.gov.hmrc.auth.core.AffinityGroup

import models.{AgentCompanyDetails, CheckRegisteredDetails, DraftId, UserAnswers}
import models.WhatIsYourRoleAsImporter.AgentOnBehalfOfOrg
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

  def agent(userAnswers: UserAnswers): ValidatedNel[Page, Option[TraderDetail]] = {
    val isAgent = userAnswers.get(WhatIsYourRoleAsImporterPage).contains(AgentOnBehalfOfOrg)

    if (isAgent) {
      userAnswers.validatedF[AgentCompanyDetails, Option[TraderDetail]](
        AgentCompanyDetailsPage,
        acd =>
          Some(
            TraderDetail(
              eori = acd.agentEori,
              businessName = acd.agentCompanyName,
              addressLine1 = acd.agentStreetAndNumber,
              addressLine2 = Some(acd.agentCity),
              addressLine3 = None,
              postcode = acd.agentPostalCode.getOrElse(""),
              countryCode = acd.agentCountry,
              phoneNumber = None
            )
          )
      )
    } else {
      Validated.Valid(None)
    }
  }

  def trader(userAnswers: UserAnswers): ValidatedNel[Page, TraderDetail] =
    userAnswers.validatedF[CheckRegisteredDetails, TraderDetail](
      CheckRegisteredDetailsPage,
      (crd: CheckRegisteredDetails) =>
        TraderDetail(
          eori = crd.eori,
          businessName = crd.name,
          addressLine1 = crd.streetAndNumber,
          addressLine2 = Some(crd.city),
          addressLine3 = None,
          postcode = crd.postalCode.getOrElse(""),
          countryCode = crd.country,
          phoneNumber = crd.phoneNumber
        )
    )
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
  private[models] val jsonConfig = JsonConfiguration(
    discriminator = "type",
    typeNaming =
      JsonNaming(fullName => fullName.slice(1 + fullName.lastIndexOf("."), fullName.length))
  )

  // TODO: Make this an OWrites as we don't need to read this model from JSON
  implicit val format: OFormat[ApplicationRequest] =
    Json.configured(jsonConfig).format[ApplicationRequest]

  def apply(
    userAnswers: UserAnswers,
    affinityGroup: AffinityGroup
  ): ValidatedNel[Page, ApplicationRequest] = {
    val traderDetail    = TraderDetail.trader(userAnswers)
    val agentDetails    = TraderDetail.agent(userAnswers)
    val goodsDetails    = GoodsDetails(userAnswers)
    val contact         = ContactDetails(userAnswers, affinityGroup)
    val requestedMethod = RequestedMethod(userAnswers)
    val attachments     = AttachmentRequest(userAnswers)

    (traderDetail, agentDetails, contact, requestedMethod, goodsDetails, attachments).mapN(
      (traderDetail, agentDetails, contact, requestedMethod, goodsDetails, attachments) =>
        ApplicationRequest(
          userAnswers.draftId,
          traderDetail,
          agentDetails,
          contact,
          requestedMethod,
          goodsDetails,
          attachments
        )
    )
  }
}
