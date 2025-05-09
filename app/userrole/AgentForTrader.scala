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
import play.api.mvc.AnyContent

import com.google.inject.Inject
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import pages.{AgentForTraderCheckRegisteredDetailsPage, Page}

package userrole {
  import cats.data.ValidatedNel
  import play.api.mvc.Call
  import play.twirl.api.HtmlFormat
  import controllers.routes.{BusinessContactDetailsController, ProvideTraderEoriController}
  import forms.AgentForTraderCheckRegisteredDetailsFormProvider
  import logging.Logging
  import models.{BusinessContactDetails, NormalMode, TraderDetailsWithConfirmation, UserAnswers}
  import models.requests.ContactDetails
  import pages.{AgentForTraderContactDetailsPage, BusinessContactDetailsPage, UploadLetterOfAuthorityPage, VerifyTraderDetailsPage}
  import viewmodels.checkAnswers.summary._
  import views.html._

  private case class AgentForTrader @Inject() (
    checkRegisteredDetailsView: VerifyPublicTraderDetailView,
    formProvider: AgentForTraderCheckRegisteredDetailsFormProvider,
    agentForTraderCheckYourAnswersView: AgentForTraderCheckYourAnswersView,
    eoriBeUpToDateViewPublic: AgentForTraderPublicEORIBeUpToDateView,
    eoriBeUpToDateViewPrivate: AgentForTraderPrivateEORIBeUpToDateView,
    requiredInformationView: AgentForTraderRequiredInformationView,
    agentSummaryCreator: AgentSummaryCreator,
    traderEoriDetailsSummaryCreator: TraderEoriDetailsSummaryCreator
  ) extends UserRole
      with Logging {
    override def selectViewForCheckRegisteredDetails(
      form: Form[Boolean],
      details: TraderDetailsWithCountryCode,
      mode: Mode,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      checkRegisteredDetailsView(form, mode, draftId, TraderDetailsWithConfirmation(details))

    override def getFormForCheckRegisteredDetails: Form[Boolean] = formProvider.apply()

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

    override def getEORIDetailsJourney(mode: Mode, draftId: DraftId): Call =
      ProvideTraderEoriController.onPageLoad(mode, draftId)

    override def contactDetailsIncludeCompanyName: Boolean = true

    override def selectBusinessContactDetailsPage(): Page = AgentForTraderContactDetailsPage

    override def selectViewForCheckYourAnswers(
      applicationSummary: ApplicationSummary,
      draftId: DraftId
    )(implicit request: DataRequest[AnyContent], messages: Messages): HtmlFormat.Appendable =
      agentForTraderCheckYourAnswersView(applicationSummary, draftId)

    override def getApplicationSummary(
      userAnswers: UserAnswers,
      traderDetailsWithCountryCode: TraderDetailsWithCountryCode
    )(implicit messages: Messages): (ApplicantSummary, EoriDetailsSummary) =
      userAnswers.get(VerifyTraderDetailsPage) match {
        case Some(traderDetailsFromUA) =>
          (
            agentSummaryCreator.summaryRows(userAnswers),
            traderEoriDetailsSummaryCreator.summaryRows(
              traderDetailsFromUA.withoutConfirmation,
              userAnswers.draftId,
              userAnswers.get(UploadLetterOfAuthorityPage).get.fileName.get
            )
          )

        case None =>
          logger.error(
            "[AgentForTrader][getApplicationSummary] VerifyTraderDetailsPage needs to be answered(getApplicationSummary)"
          )
          throw new Exception("VerifyTraderDetailsPage needs to be answered(getApplicationSummary)")

      }

    override def getContactDetailsForApplicationRequest(
      userAnswers: UserAnswers
    ): ValidatedNel[Page, ContactDetails] =
      userAnswers.validatedF[BusinessContactDetails, ContactDetails](
        BusinessContactDetailsPage,
        cd => ContactDetails(cd.name, cd.email, Some(cd.phone), cd.companyName, Some(cd.jobTitle))
      )

    override val getMaxSupportingDocuments: Int = 4

    override def sourceFromUA: Boolean = true

    override def getContactDetailsJourney(draftId: DraftId): Call =
      BusinessContactDetailsController.onPageLoad(NormalMode, draftId)
  }

}
