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

  def apply(userAnswers: UserAnswers): ValidatedNel[Page, ApplicationRequest] = {
    val applicationNumber = userAnswers.applicationNumber
    val goodsDetails      = GoodsDetails(userAnswers)
    val applicant         = Applicant(userAnswers)
    val requestedMethod   = RequestedMethod(userAnswers)

    // val attachments       = userAnswers.get(AttachmentsPage).getOrElse(Seq.empty)

    (goodsDetails, applicant, requestedMethod).mapN(
      (goodsDetails, applicant, requestedMethod) =>
        ApplicationRequest(
          applicationNumber,
          applicant,
          requestedMethod,
          goodsDetails,
          Seq.empty
        )
    )
  }
}
