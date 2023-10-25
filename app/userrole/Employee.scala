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
  import controllers.routes.ApplicationContactDetailsController
  import forms.EmployeeCheckRegisteredDetailsFormProvider
  import models.{ApplicationContactDetails, NormalMode, UserAnswers}
  import models.requests.ContactDetails
  import pages.{ApplicationContactDetailsPage, CheckRegisteredDetailsPage, Page, ValuationMethodPage}
  import viewmodels.checkAnswers.summary.{ApplicantSummary, ApplicationSummary, EoriDetailsSummary, IndividualApplicantSummary, IndividualEoriDetailsSummary}
  import views.html.{CheckYourAnswersView, EmployeeCheckRegisteredDetailsView, EmployeeEORIBeUpToDateView, IndividualInformationRequiredView}

  private case class Employee @Inject() (
    checkRegisteredDetailsView: EmployeeCheckRegisteredDetailsView,
    formProvider: EmployeeCheckRegisteredDetailsFormProvider,
    checkYourAnswersView: CheckYourAnswersView,
    eoriBeUpToDateView: EmployeeEORIBeUpToDateView,
    requiredInformationRequiredView: IndividualInformationRequiredView
  ) extends UserRole {
    override def selectViewForCheckRegisteredDetails(
      form: Form[Boolean],
      details: TraderDetailsWithCountryCode,
      mode: Mode,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      checkRegisteredDetailsView(
        form,
        details,
        mode,
        draftId
      )

    override def getFormForCheckRegisteredDetails: Form[Boolean] = formProvider.apply()

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

    override def contactDetailsIncludeCompanyName: Boolean = false

    override def selectBusinessContactDetailsPage(): Page = ValuationMethodPage

    override def selectViewForCheckYourAnswers(
      applicationSummary: ApplicationSummary,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      checkYourAnswersView(applicationSummary, draftId)

    override def getApplicationSummary(
      userAnswers: UserAnswers,
      traderDetailsWithCountryCode: TraderDetailsWithCountryCode
    )(implicit messages: Messages): (ApplicantSummary, EoriDetailsSummary) =
      (
        IndividualApplicantSummary(userAnswers),
        IndividualEoriDetailsSummary(traderDetailsWithCountryCode, userAnswers.draftId, userAnswers)
      )

    override def getContactDetailsForApplicationRequest(
      userAnswers: UserAnswers
    ): ValidatedNel[Page, ContactDetails] =
      userAnswers
        .validatedF[ApplicationContactDetails, ContactDetails](
          ApplicationContactDetailsPage,
          cd => ContactDetails(cd.name, cd.email, Some(cd.phone), None, Some(cd.jobTitle))
        )

    override val getMaxSupportingDocuments: Int = 5

    override def sourceFromUA: Boolean = false

    override def getContactDetailsJourney(draftId: DraftId): Call =
      ApplicationContactDetailsController.onPageLoad(NormalMode, draftId)
  }
}
