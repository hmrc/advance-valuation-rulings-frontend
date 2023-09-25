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

import cats.data._
import cats.implicits._

import play.api.libs.json._

import com.google.inject.Inject
import models.{AgentCompanyDetails, DraftId, TraderDetailsWithCountryCode, UserAnswers}
import models.UploadedFile.{Success, UploadDetails}
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

  def agent(userAnswers: UserAnswers): ValidatedNel[QuestionPage[_], Option[TraderDetail]] =
    userAnswers.get(WhatIsYourRoleAsImporterPage) match {
      case Some(AgentOnBehalfOfOrg) =>
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
                countryCode = acd.agentCountry.code,
                phoneNumber = None
              )
            )
        )

      case _ => Validated.Valid(None)
    }

  def trader(
    userAnswers: UserAnswers,
    crd: TraderDetailsWithCountryCode
  ): ValidatedNel[Page, TraderDetail] = {

    val registeredDetails = userAnswers
      .validated(CheckRegisteredDetailsPage)
      .ensure(NonEmptyList.one(CheckRegisteredDetailsPage))(_ == true)

    registeredDetails.map(
      _ =>
        TraderDetail(
          eori = crd.EORINo,
          businessName = crd.CDSFullName,
          addressLine1 = crd.CDSEstablishmentAddress.streetAndNumber,
          addressLine2 = Some(crd.CDSEstablishmentAddress.city),
          addressLine3 = None,
          postcode = crd.CDSEstablishmentAddress.postalCode.getOrElse(""),
          countryCode = crd.CDSEstablishmentAddress.countryCode,
          phoneNumber = crd.contactInformation.flatMap(_.telephoneNumber)
        )
    )
  }
}

case class ApplicationRequest(
  draftId: DraftId,
  trader: TraderDetail,
  agent: Option[TraderDetail],
  contact: ContactDetails,
  requestedMethod: RequestedMethod,
  goodsDetails: GoodsDetails,
  attachments: Seq[AttachmentRequest],
  whatIsYourRole: WhatIsYourRole,
  letterOfAuthority: Option[AttachmentRequest]
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
}

class ApplicationRequestService @Inject() (
  contactDetailsService: ContactDetailsService
) {
  def apply(
    userAnswers: UserAnswers,
    traderDetailsWithCountryCode: TraderDetailsWithCountryCode
  ): ValidatedNel[Page, ApplicationRequest] = {
    val agentDetails                   = TraderDetail.agent(userAnswers)
    val traderDetail                   = TraderDetail.trader(userAnswers, traderDetailsWithCountryCode)
    val goodsDetails                   = GoodsDetails(userAnswers)
    val contact                        = contactDetailsService(userAnswers)
    val requestedMethod                = RequestedMethod(userAnswers)
    val attachments                    = AttachmentRequest(userAnswers)
    val whatIsYourRole: WhatIsYourRole = WhatIsYourRole(userAnswers)
    val loa: Option[AttachmentRequest] = letterOfAuthority(userAnswers)

    (
      traderDetail,
      agentDetails,
      contact,
      requestedMethod,
      goodsDetails,
      attachments
    )
      .mapN(
        (
          traderDetail,
          agentDetails,
          contact,
          requestedMethod,
          goodsDetails,
          attachments
        ) =>
          ApplicationRequest(
            userAnswers.draftId,
            traderDetail,
            agentDetails,
            contact,
            requestedMethod,
            goodsDetails,
            attachments,
            whatIsYourRole,
            loa
          )
      )
      .leftMap( // Removing duplicates whilst retaining order
        ps =>
          ps.tail.foldLeft(NonEmptyList.of(ps.head))(
            (acc, next) => if (acc.exists(p => p == next)) acc else acc :+ next
          )
      )
  }

  def letterOfAuthority(userAnswers: UserAnswers): Option[AttachmentRequest] =
    userAnswers.get(UploadLetterOfAuthorityPage) match {
      case Some(Success(_: String, downloadUrl: String, uploadDetails: UploadDetails)) =>
        Some(
          AttachmentRequest(
            name = uploadDetails.fileName,
            description = None,
            url = downloadUrl,
            privacy = Privacy.Public,
            mimeType = uploadDetails.fileMimeType,
            size = uploadDetails.size
          )
        )
      case _                                                                           => None
    }
}
