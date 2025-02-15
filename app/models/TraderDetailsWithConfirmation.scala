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

package models

import play.api.libs.json.{Json, OFormat}

final case class TraderDetailsWithConfirmation(
  EORINo: String,
  consentToDisclosureOfPersonalData: Boolean,
  CDSFullName: String,
  CDSEstablishmentAddress: CDSEstablishmentAddress,
  contactInformation: Option[ContactInformation],
  confirmation: Option[Boolean]
) {
  def withoutConfirmation: TraderDetailsWithCountryCode =
    TraderDetailsWithCountryCode(
      EORINo,
      consentToDisclosureOfPersonalData,
      CDSFullName,
      CDSEstablishmentAddress,
      contactInformation
    )
}

object TraderDetailsWithConfirmation {
  def apply(details: TraderDetailsWithCountryCode): TraderDetailsWithConfirmation =
    new TraderDetailsWithConfirmation(
      details.EORINo,
      details.consentToDisclosureOfPersonalData,
      details.CDSFullName,
      details.CDSEstablishmentAddress,
      details.contactInformation,
      None
    )

  given format: OFormat[TraderDetailsWithConfirmation] =
    Json.format[TraderDetailsWithConfirmation]
}
