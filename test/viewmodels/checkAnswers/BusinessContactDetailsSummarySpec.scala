/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.checkAnswers

import base.SpecBase
import controllers.routes
import models.WhatIsYourRoleAsImporter.AgentOnBehalfOfTrader
import models.{BusinessContactDetails, CheckMode, UserAnswers}
import pages.{BusinessContactDetailsPage, WhatIsYourRoleAsImporterPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class BusinessContactDetailsSummarySpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  private val businessContactDetails: BusinessContactDetails = BusinessContactDetails(
    name = "name",
    email = "abc@email.com",
    phone = "0123456789",
    companyName = Some("company name"),
    jobTitle = "CEO"
  )
  private val userAnswers: UserAnswers                       = userAnswersAsIndividualTrader
    .set(BusinessContactDetailsPage, businessContactDetails)
    .success
    .value
    .set(WhatIsYourRoleAsImporterPage, AgentOnBehalfOfTrader)
    .success
    .value

  ".rows" - {

    "must create rows for BusinessContactDetailsSummary" in {

      BusinessContactDetailsSummary.rows(userAnswers) mustBe Some(
        Seq(
          SummaryListRowViewModel(
            key = "agentForTraderCheckYourAnswers.applicant.name.label",
            value = ValueViewModel(HtmlContent(businessContactDetails.name)),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
              ).withVisuallyHiddenText(messages("agentForTraderCheckYourAnswers.applicant.name.hidden"))
            )
          ),
          SummaryListRowViewModel(
            key = "agentForTraderCheckYourAnswers.applicant.email.label",
            value = ValueViewModel(HtmlFormat.escape(businessContactDetails.email).toString),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
              ).withVisuallyHiddenText(messages("agentForTraderCheckYourAnswers.applicant.email.hidden"))
            )
          ),
          SummaryListRowViewModel(
            key = "agentForTraderCheckYourAnswers.applicant.phone.label",
            value = ValueViewModel(HtmlFormat.escape(businessContactDetails.phone).toString),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
              ).withVisuallyHiddenText(messages("agentForTraderCheckYourAnswers.applicant.phone.hidden"))
            )
          ),
          SummaryListRowViewModel(
            key = "agentForTraderCheckYourAnswers.applicant.companyName.label",
            value = ValueViewModel(HtmlFormat.escape(businessContactDetails.companyName.getOrElse("")).toString),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
              ).withVisuallyHiddenText(
                messages("agentForTraderCheckYourAnswers.applicant.companyName.hidden")
              )
            )
          ),
          SummaryListRowViewModel(
            key = "agentForTraderCheckYourAnswers.applicant.jobTitle.label",
            value = ValueViewModel(HtmlFormat.escape(businessContactDetails.jobTitle).toString),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.BusinessContactDetailsController.onPageLoad(CheckMode, draftId).url
              )
                .withVisuallyHiddenText(messages("agentForTraderCheckYourAnswers.applicant.jobTitle.hidden"))
            )
          )
        )
      )
    }
  }
}
