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

import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import controllers.routes
import models.{CheckMode, CheckRegisteredDetails, UserAnswers}
import models.requests._
import pages.CheckRegisteredDetailsPage
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CheckRegisteredDetailsSummary {

  private def registeredNameRow(answer: CheckRegisteredDetails)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswers.eori.name.label",
      value = ValueViewModel(HtmlFormat.escape(answer.name).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.change.hidden"))
      )
    )

  private def registeredAddressRow(answer: CheckRegisteredDetails)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswers.eori.address.label",
      value = ValueViewModel(
        HtmlContent(
          Html(
            s"${HtmlFormat.escape(answer.streetAndNumber).body}<br>" +
              s"${HtmlFormat.escape(answer.city).body}<br>" +
              answer.postalCode
                .map(value => s"${HtmlFormat.escape(value).body}<br>")
                .getOrElse("") +
              s"${HtmlFormat.escape(answer.country).body}"
          )
        )
      ),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.change.hidden"))
      )
    )

  private def registeredNumberRow(answer: CheckRegisteredDetails)(implicit
    messages: Messages
  ): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkYourAnswers.eori.number.label",
      value = ValueViewModel(HtmlFormat.escape(answer.eori).body),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.CheckRegisteredDetailsController.onPageLoad(CheckMode).url
        )
          .withVisuallyHiddenText(messages("checkRegisteredDetails.change.hidden"))
      )
    )

  def rows(userAnswer: UserAnswers)(implicit messages: Messages): Option[Seq[SummaryListRow]] =
    for {
      contactDetails <- userAnswer.get(CheckRegisteredDetailsPage)
      number          = registeredNumberRow(contactDetails)
      name            = registeredNameRow(contactDetails)
      address         = registeredAddressRow(contactDetails)
      result          = Seq(number, name, address)
    } yield result

  def rows(
    application: Application
  )(implicit messages: Messages): Seq[SummaryListRow] = {

    val postCode       =
      if (application.trader.postcode.isEmpty) None else Some(application.trader.postcode)
    val contactDetails = models.CheckRegisteredDetails(
      value = true,
      eori = application.trader.eori,
      name = application.trader.businessName,
      streetAndNumber = application.trader.addressLine1,
      city = application.trader.addressLine2.getOrElse(""),
      country = application.trader.countryCode,
      postalCode = postCode,
      phoneNumber = application.trader.phoneNumber
    )
    Seq(
      registeredNumberRow(contactDetails),
      registeredNameRow(contactDetails),
      registeredAddressRow(contactDetails)
    )
  }
}
