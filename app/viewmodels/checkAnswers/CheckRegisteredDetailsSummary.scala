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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, DraftId, EoriNumber, UserAnswers}
import pages.CheckRegisteredDetailsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CheckRegisteredDetailsSummary {

  private def registeredNameRow(answer: Boolean, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswers.eori.name.label",
      value = ValueViewModel(HtmlFormat.escape(???).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode, draftId).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.name.change.hidden"))
      )
    )

  private def registeredAddressRow(answer: Boolean, draftId: DraftId)(implicit
    messages: Messages
  ): SummaryListRow = ???

  private def registeredNumberRow(eoriNumber: EoriNumber, draftId: DraftId)(implicit
    messages: Messages
  ) = ???

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(CheckRegisteredDetailsPage)
      number          = registeredNumberRow(EoriNumber(???), userAnswer.draftId)
    } yield number +: getPersonalDetails(???, userAnswer.draftId)

  private def getPersonalDetails(
    registeredDetails: Boolean,
    draftId: DraftId
  )(implicit messages: Messages) =
    if (???) {
      val name    = registeredNameRow(registeredDetails, draftId)
      val address = registeredAddressRow(registeredDetails, draftId)
      Seq(name, address)
    } else {
      Nil
    }
}
