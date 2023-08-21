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
import play.api.mvc.{AnyContent, Call}
import play.twirl.api.HtmlFormat

import com.google.inject.Inject
import models.{DraftId, Mode, TraderDetailsWithCountryCode}
import models.requests.DataRequest
import pages.{CheckRegisteredDetailsPage, Page}
import views.html.{AgentForOrgCheckRegisteredDetailsView, AgentForOrgEORIBeUpToDateView, AgentForOrgRequiredInformationView}

package userrole {

  import controllers.routes.{ApplicationContactDetailsController, CheckRegisteredDetailsController}
  import models.NormalMode
  import pages.AgentForOrgApplicationContactDetailsPage

  private case class AgentForOrg @Inject() (
    view: AgentForOrgCheckRegisteredDetailsView,
    eoriBeUpToDateView: AgentForOrgEORIBeUpToDateView,
    requiredInformation: AgentForOrgRequiredInformationView
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
      requiredInformation(draftId)
    override def getEORIDetailsJourney(draftId: DraftId): Call =
      CheckRegisteredDetailsController.onPageLoad(NormalMode, draftId)

    override def contactDetailsIncludeCompanyName: Boolean = false

    override def selectApplicationContactDetailsPage(): Page =
      AgentForOrgApplicationContactDetailsPage
  }
}
