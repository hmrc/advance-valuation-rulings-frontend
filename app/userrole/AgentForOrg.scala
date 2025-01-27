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

import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}

import com.google.inject.Inject
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import pages.Page
import views.html.{AgentForOrgCheckRegisteredDetailsView, AgentForOrgEORIBeUpToDateView, AgentForOrgRequiredInformationView}

package userrole {

  import cats.data.ValidatedNel
  import play.twirl.api.HtmlFormat
  import controllers.routes.{BusinessContactDetailsController, CheckRegisteredDetailsController}
  import forms.AgentForOrgCheckRegisteredDetailsFormProvider
  import models.{BusinessContactDetails, NormalMode, UserAnswers}
  import models.requests.ContactDetails
  import pages.{AgentForOrgApplicationContactDetailsPage, AgentForOrgCheckRegisteredDetailsPage, BusinessContactDetailsPage}
  import viewmodels.checkAnswers.summary._
  import views.html.AgentForOrgCheckYourAnswersView

  private case class AgentForOrg @Inject() (
    checkRegisteredDetailsView: AgentForOrgCheckRegisteredDetailsView,
    formProvider: AgentForOrgCheckRegisteredDetailsFormProvider,
    agentForOrgCheckYourAnswersView: AgentForOrgCheckYourAnswersView,
    eoriBeUpToDateView: AgentForOrgEORIBeUpToDateView,
    requiredInformation: AgentForOrgRequiredInformationView,
    agentSummaryCreator: AgentSummaryCreator,
    businessEoriDetailsSummaryCreator: BusinessEoriDetailsSummaryCreator
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

    override def selectGetRegisteredDetailsPage(): Page = AgentForOrgCheckRegisteredDetailsPage

    override def selectViewForRequiredInformation(
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      requiredInformation(draftId)
    override def getEORIDetailsJourney(mode: Mode, draftId: DraftId): Call =
      CheckRegisteredDetailsController.onPageLoad(mode, draftId)

    override def contactDetailsIncludeCompanyName: Boolean = false

    override def selectBusinessContactDetailsPage(): Page =
      AgentForOrgApplicationContactDetailsPage

    override def selectViewForCheckYourAnswers(
      applicationSummary: ApplicationSummary,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      agentForOrgCheckYourAnswersView(applicationSummary, draftId)

    override def getApplicationSummary(
      userAnswers: UserAnswers,
      traderDetailsWithCountryCode: TraderDetailsWithCountryCode
    )(implicit messages: Messages): (ApplicantSummary, EoriDetailsSummary) =
      (
        agentSummaryCreator.summaryRows(userAnswers),
        businessEoriDetailsSummaryCreator.summaryRows(
          traderDetailsWithCountryCode,
          userAnswers.draftId
        )
      )

    override def getContactDetailsForApplicationRequest(
      userAnswers: UserAnswers
    ): ValidatedNel[Page, ContactDetails] =
      userAnswers.validatedF[BusinessContactDetails, ContactDetails](
        BusinessContactDetailsPage,
        cd => ContactDetails(cd.name, cd.email, Some(cd.phone), None, Some(cd.jobTitle))
      )

    override val getMaxSupportingDocuments: Int = 5

    override def sourceFromUA: Boolean = false

    override def getContactDetailsJourney(draftId: DraftId): Call =
      BusinessContactDetailsController.onPageLoad(NormalMode, draftId)

  }
}
