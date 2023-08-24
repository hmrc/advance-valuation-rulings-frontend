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

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent

import com.google.inject.Inject
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
package userrole {

  import cats.data.ValidatedNel

  import play.api.mvc.Call
  import play.twirl.api.HtmlFormat

  import controllers.routes
  import models.{ApplicationContactDetails, NormalMode, UserAnswers}
  import models.requests.ContactDetails
  import pages.{ApplicationContactDetailsPage, CheckRegisteredDetailsPage, Page}
  import viewmodels.checkAnswers.summary.{ApplicantSummary, ApplicationSummary, BusinessEoriDetailsSummary, EoriDetailsSummary, IndividualApplicantSummary}
  import views.html.{CheckYourAnswersView, EmployeeCheckRegisteredDetailsView, EmployeeEORIBeUpToDateView, IndividualInformationRequiredView}

  private case class Employee @Inject() (
    view: EmployeeCheckRegisteredDetailsView,
    eoriBeUpToDateView: EmployeeEORIBeUpToDateView,
    requiredInformationRequiredView: IndividualInformationRequiredView,
    checkYourAnswersView: CheckYourAnswersView
  ) extends UserRole {
    override def selectViewForCheckRegisteredDetails(
      form: Form[Boolean],
      details: TraderDetailsWithCountryCode,
      mode: Mode,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      view(
        form,
        details,
        mode,
        draftId
      )

    override def selectViewForEoriBeUpToDate(
      draftId: DraftId,
      isPrivate: Boolean = false
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      eoriBeUpToDateView(draftId)

    override def selectGetRegisteredDetailsPage(): Page = CheckRegisteredDetailsPage

    override def selectViewForRequiredInformation(
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      requiredInformationRequiredView(draftId)
    override def getEORIDetailsJourney(draftId: DraftId): Call =
      routes.CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId)

    override def selectBusinessContactDetailsPage(): Page = ApplicationContactDetailsPage

    override def selectViewForCheckYourAnswers(
      applicationSummary: ApplicationSummary,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      checkYourAnswersView(applicationSummary, draftId)

    override def getApplicationSummary(
      userAnswers: UserAnswers,
      traderDetailsWithCountryCode: TraderDetailsWithCountryCode
    )(implicit messages: Messages): (ApplicantSummary, EoriDetailsSummary) = (
      IndividualApplicantSummary(userAnswers),
      BusinessEoriDetailsSummary(traderDetailsWithCountryCode, userAnswers.draftId)
    )

    override def getContactDetailsForApplicationRequest(
      userAnswers: UserAnswers
    ): ValidatedNel[Page, ContactDetails] =
      userAnswers
        .validatedF[ApplicationContactDetails, ContactDetails](
          ApplicationContactDetailsPage,
          cd => ContactDetails(cd.name, cd.email, Some(cd.phone))
        )

  }

}
