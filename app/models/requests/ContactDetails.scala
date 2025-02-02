/*
 * Copyright 2025 HM Revenue & Customs
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
import com.google.inject.Inject
import models._
import pages._
import play.api.libs.json._
import userrole.UserRoleProvider

case class CompanyContactDetails(
  name: String,
  email: String,
  phone: Option[String]
)
object CompanyContactDetails {
  given format: OFormat[CompanyContactDetails] = Json.format[CompanyContactDetails]
}

case class ContactDetails(
  name: String,
  email: String,
  phone: Option[String],
  companyName: Option[String],
  jobTitle: Option[String]
)

object ContactDetails {
  given format: OFormat[ContactDetails] = Json.format[ContactDetails]
}

class ContactDetailsService @Inject() (
  userRoleProvider: UserRoleProvider
) {

  given format: OFormat[ContactDetails] = Json.format[ContactDetails]

  def apply(answers: UserAnswers): ValidatedNel[Page, ContactDetails] =
    userRoleProvider
      .getUserRole(answers)
      .getContactDetailsForApplicationRequest(answers)

}
