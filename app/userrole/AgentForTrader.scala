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
import pages.{AgentForTraderCheckRegisteredDetailsPage, Page}
import views.html.AgentForTraderCheckRegisteredDetailsView

package userrole {
  import cats.data.ValidatedNel

  import play.api.mvc.Call
  import play.twirl.api.HtmlFormat

  import controllers.routes.ProvideTraderEoriController
  import models.{BusinessContactDetails, UserAnswers}
  import models.requests.ContactDetails
  import pages.{BusinessContactDetailsPage, ValuationMethodPage}
  import viewmodels.checkAnswers.summary.{AgentSummary, ApplicantSummary, ApplicationSummary, BusinessEoriDetailsSummary, EoriDetailsSummary, IndividualApplicantSummary, TraderEoriDetailsSummary}
  import views.html.{AgentForTraderCheckYourAnswersView, AgentForTraderPrivateEORIBeUpToDateView, AgentForTraderPublicEORIBeUpToDateView, AgentForTraderRequiredInformationView}

  private case class AgentForTrader @Inject() (
    checkRegisteredDetailsView: AgentForTraderCheckRegisteredDetailsView,
    agentForTraderCheckYourAnswersView: AgentForTraderCheckYourAnswersView,
    eoriBeUpToDateViewPublic: AgentForTraderPublicEORIBeUpToDateView,
    eoriBeUpToDateViewPrivate: AgentForTraderPrivateEORIBeUpToDateView,
    requiredInformationView: AgentForTraderRequiredInformationView
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
    override def selectViewForEoriBeUpToDate(
      draftId: DraftId,
      isPrivate: Boolean = false
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      if (isPrivate) eoriBeUpToDateViewPrivate(draftId) else eoriBeUpToDateViewPublic(draftId)

    override def selectGetRegisteredDetailsPage(): Page = AgentForTraderCheckRegisteredDetailsPage

    override def selectViewForRequiredInformation(
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      requiredInformationView(draftId)

    override def getEORIDetailsJourney(draftId: DraftId): Call =
      ProvideTraderEoriController.onPageLoad(draftId)

    override def contactDetailsIncludeCompanyName: Boolean = true

    override def selectBusinessContactDetailsPage(): Page = ValuationMethodPage

    override def selectViewForCheckYourAnswers(
      applicationSummary: ApplicationSummary,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      agentForTraderCheckYourAnswersView(applicationSummary, draftId)

    override def getApplicationSummary(
      userAnswers: UserAnswers,
      traderDetailsWithCountryCode: TraderDetailsWithCountryCode
    )(implicit messages: Messages): (ApplicantSummary, EoriDetailsSummary) =
      (
        AgentSummary(userAnswers),
        TraderEoriDetailsSummary(traderDetailsWithCountryCode, userAnswers.draftId)
      )

    override def getContactDetailsForApplicationRequest(
      userAnswers: UserAnswers
    ): ValidatedNel[Page, ContactDetails] =
      userAnswers.validatedF[BusinessContactDetails, ContactDetails](
        BusinessContactDetailsPage,
        cd => ContactDetails(cd.name, cd.email, Some(cd.phone), cd.companyName)
      )

  }

}
