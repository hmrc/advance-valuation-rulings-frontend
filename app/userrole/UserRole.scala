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

package userrole

import cats.data.ValidatedNel

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import play.twirl.api.HtmlFormat

import models.{DraftId, Mode, TraderDetailsWithCountryCode, UserAnswers}
import models.requests.{ContactDetails, DataRequest}
import pages.Page
import viewmodels.checkAnswers.summary.{ApplicantSummary, ApplicationSummary, EoriDetailsSummary}

trait UserRole {
  def selectViewForEoriBeUpToDate(
    draftId: DraftId,
    isPrivate: Boolean = false
  )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable

  def selectViewForCheckRegisteredDetails(
    form: Form[Boolean],
    details: TraderDetailsWithCountryCode,
    mode: Mode,
    draftId: DraftId
  )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable

  def getFormForCheckRegisteredDetails: Form[Boolean]

  def selectGetRegisteredDetailsPage(): Page

  def selectBusinessContactDetailsPage(): Page

  def selectViewForRequiredInformation(
    draftId: DraftId
  )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable
  def getEORIDetailsJourney(draftId: DraftId): Call

  def contactDetailsIncludeCompanyName: Boolean

  def selectViewForCheckYourAnswers(
    applicationSummary: ApplicationSummary,
    draftId: DraftId
  )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable

  def getApplicationSummary(
    userAnswers: UserAnswers,
    traderDetailsWithCountryCode: TraderDetailsWithCountryCode
  )(implicit messages: Messages): (ApplicantSummary, EoriDetailsSummary)

  def getContactDetailsForApplicationRequest(
    userAnswers: UserAnswers
  ): ValidatedNel[Page, ContactDetails]

  def getMaxSupportingDocuments: Int

  def sourceFromUA: Boolean

  def getContactDetailsJourney(draftId: DraftId): Call
}
