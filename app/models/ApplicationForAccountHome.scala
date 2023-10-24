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

package models

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

import controllers.routes
import models.requests._

final case class ApplicationForAccountHome(
  id: String,
  goodsName: String,
  date: Instant,
  statusTag: Tag,
  actions: Seq[ActionItem]
) {
  def dateString(lang: Lang = Lang("en")): String =
    if (lang == Lang("en")) {
      ApplicationForAccountHome.formatter.format(date)
    } else {
      ApplicationForAccountHome.welshFormatter.format(date)
    }
}

object ApplicationForAccountHome {

  private val formatter =
    DateTimeFormatter
      .ofPattern("dd MMMM yyyy")
      .withZone(ZoneId.systemDefault())

  private val welshFormatter =
    DateTimeFormatter
      .ofPattern("dd MMMM yyyy")
      .withZone(ZoneId.systemDefault())
      .withLocale(new Locale("cy", "GB"))

  def apply(applicationSummary: ApplicationSummary)(implicit
    messages: Messages
  ): ApplicationForAccountHome =
    ApplicationForAccountHome(
      id = applicationSummary.id.toString,
      goodsName = applicationSummary.goodsName,
      date = applicationSummary.dateSubmitted,
      statusTag =
        Tag(content = Text(messages("accountHome.status.submitted")), classes = "govuk-tag--blue"),
      actions = Seq(
        ActionItem(
          href = routes.ViewApplicationController.onPageLoad(applicationSummary.id.toString).url,
          content = Text(messages("accountHome.viewApplication"))
        )
      )
    )

  def apply(draftSummary: DraftSummary, continueCall: Call)(implicit
    messages: Messages
  ): ApplicationForAccountHome =
    ApplicationForAccountHome(
      id = draftSummary.id.toString,
      goodsName = draftSummary.goodsName.getOrElse(""),
      date = draftSummary.lastUpdated,
      statusTag =
        Tag(content = Text(messages("accountHome.status.draft")), classes = "govuk-tag--yellow"),
      actions = Seq(
        ActionItem(
          href = continueCall.url,
          content = Text(messages("accountHome.continueApplication"))
        ),
        ActionItem(
          href = routes.DeleteDraftController.onPageLoad(draftSummary.id).url,
          content = Text(messages("accountHome.deleteApplication"))
        )
      )
    )
}
