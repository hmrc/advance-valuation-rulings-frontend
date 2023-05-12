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
import cats.data.Validated._

import play.api.libs.json._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

import models._
import models.AuthUserType.IndividualTrader
import models.AuthUserType.OrganisationAdmin
import models.AuthUserType.OrganisationAssistant
import pages._

case class CompanyContactDetails(
  name: String,
  email: String,
  phone: Option[String]
)
object CompanyContactDetails {
  implicit val format: OFormat[CompanyContactDetails] = Json.format[CompanyContactDetails]
}

case class ContactDetails(
  name: String,
  email: String,
  phone: Option[String]
)
object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]

  def apply(
    answers: UserAnswers
  ): ValidatedNel[Page, ContactDetails] =
    answers
      .validated(AccountHomePage)
      .andThen(
        authUserType =>
          authUserType match {
            case IndividualTrader      =>
              answers.validatedF[ApplicationContactDetails, ContactDetails](
                ApplicationContactDetailsPage,
                cd => ContactDetails(cd.name, cd.email, Some(cd.phone))
              )
            case OrganisationAdmin     =>
              answers
                .validatedF[ApplicationContactDetails, ContactDetails](
                  ApplicationContactDetailsPage,
                  cd => ContactDetails(cd.name, cd.email, Some(cd.phone))
                )
            case OrganisationAssistant =>
              answers
                .validatedF[BusinessContactDetails, ContactDetails](
                  BusinessContactDetailsPage,
                  cd => ContactDetails(cd.name, cd.email, Some(cd.phone))
                )
          }
      )

}
