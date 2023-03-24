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

import cats.data.EitherNel
import cats.implicits._

import play.api.libs.json._

import enumeratum.Enum
import models.BusinessContactDetails
import models.UserAnswers
import pages._

case class UploadedDocument(
  id: String,
  name: String,
  url: String,
  public: Boolean, // isConfidential
  mimeType: String,
  size: Long
)
object UploadedDocument {
  implicit val format: OFormat[UploadedDocument] = Json.format[UploadedDocument]
}

sealed trait Applicant
case class IndividualApplicant(
  holder: EORIDetails,
  contact: ContactDetails
) extends Applicant

case class BusinessApplicant(
  holder: EORIDetails,
  businessContact: BusinessContactDetails
) extends Applicant

object BusinessApplicant {
  implicit val format: OFormat[BusinessApplicant] = Json.format[BusinessApplicant]
}

object Applicant {
  import ApplicationRequest._
  implicit val roleFormat: OFormat[Applicant] = Json.configured(jsonConfig).format[Applicant]

  def eoriHolder: Applicant => EORIDetails = (applicant: Applicant) =>
    applicant match {
      case IndividualApplicant(holder, _) => holder
      case BusinessApplicant(holder, _)   => holder
    }

  def contactDetails: Applicant => Option[ContactDetails] = {
    case IndividualApplicant(_, contact) => Some(contact)
    case BusinessApplicant(_, _)         => None
  }

  def businessContactDetails: Applicant => Option[BusinessContactDetails] = {
    case IndividualApplicant(_, _)     => None
    case BusinessApplicant(_, contact) => Some(contact)
  }
}

object IndividualApplicant {
  implicit val format: OFormat[IndividualApplicant] = Json.format[IndividualApplicant]
}

case class GoodsDetails(
  goodName: String,
  goodDescription: String,
  envisagedCommodityCode: Option[String],
  knownLegalProceedings: Option[String],
  confidentialInformation: Option[String]
)

object GoodsDetails {
  implicit val format: OFormat[GoodsDetails]                                    = Json.format[GoodsDetails]

  def apply(userAnswers: UserAnswers): EitherNel[QuestionPage[_], GoodsDetails] = {
    val name        = userAnswers.get(DescriptionOfGoodsPage)
    val description = userAnswers.get(DescriptionOfGoodsPage)

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

    GoodsDetails(
      goodName = name.getOrElse(""),
      goodDescription = description.getOrElse(""),
      envisagedCommodityCode = envisagedCommodityCode,
      knownLegalProceedings = knownLegalProceedings,
      confidentialInformation = confidentialInformation
    ).asRight
  }
}

case class EORIDetails(
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
}

case class ContactDetails(
  name: String,
  email: String,
  phone: Option[String]
)
object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

case class ApplicationRequest(
  applicationNumber: String,
  applicant: Applicant,
  requestedMethod: RequestedMethod,
  goodsDetails: GoodsDetails,
  attachments: Seq[UploadedDocument]
)

object ApplicationRequest {
  private[models] val jsonConfig                   = JsonConfiguration(
    discriminator = "_type",
    typeNaming =
      JsonNaming(fullName => fullName.slice(1 + fullName.lastIndexOf("."), fullName.length))
  )
  implicit val format: OFormat[ApplicationRequest] =
    Json.configured(jsonConfig).format[ApplicationRequest]

  def apply(userAnswers: UserAnswers): EitherNel[Page[_], ApplicationRequest] = {
    val applicationNumber = userAnswers.applicationNumber
    val goodsDetails      = GoodsDetails(userAnswers)
    // val applicationNumber = userAnswers.get(ApplicationNumberPage).get
    // val applicant         = userAnswers.get(ApplicantPage).get
    // val requestedMethod   = userAnswers.get(RequestedMethodPage).get
    // val goodsDetails      = userAnswers.get(GoodsDetailsPage).get
    // val attachments       = userAnswers.get(AttachmentsPage).getOrElse(Seq.empty)

    for {
      goodsDetails <- GoodsDetails(userAnswers)
    } yield ApplicationRequest(
      applicationNumber,
      ???,
      ???,
      goodsDetails,
      Seq.empty
    )
  }
}
