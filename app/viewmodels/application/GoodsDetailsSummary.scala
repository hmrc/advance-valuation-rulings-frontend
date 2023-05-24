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

package viewmodels.application

import cats.implicits.catsSyntaxOptionId

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Content, TableRow}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTable
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table

import models.requests.{Attachment, GoodsDetails}
import models.requests.Privacy.Confidential
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object GoodsDetailsSummary {
  def rows(goodsDetails: GoodsDetails, attachments: Seq[Attachment])(implicit
    messages: Messages
  ): Seq[SummaryListRow] = {

    val descriptionRow = Some(
      SummaryListRowViewModel(
        key = "descriptionOfGoods.checkYourAnswersLabel",
        value = ValueViewModel(goodsDetails.goodsName)
      )
    )

    val commodityCodeRow = goodsDetails.envisagedCommodityCode.map {
      commodityCode =>
        SummaryListRowViewModel(
          key = "commodityCode.checkYourAnswersLabel",
          value = ValueViewModel(commodityCode)
        )
    }

    val legalChallengeRow = goodsDetails.knownLegalProceedings.map {
      challenge =>
        SummaryListRowViewModel(
          key = "describeTheLegalChallenges.checkYourAnswersLabel",
          value = ValueViewModel(challenge)
        )
    }

    val confidentialInformationRow: Option[SummaryListRow] =
      goodsDetails.confidentialInformation.map {
        info =>
          SummaryListRowViewModel(
            key = "confidentialInformation.checkYourAnswersLabel",
            value = ValueViewModel(info)
          )
      }

    val attachmentsRow: Option[SummaryListRow] = if (attachments.nonEmpty) {

      val z = Table(rows = attachments.zipWithIndex.map {
        case (attachment, i) =>
          Seq(
            TableRow(content = Text(attachment.name)),
            TableRow(content =
              Text(
                if (attachment.privacy == Confidential)
                  messages("uploadAnotherSupportingDocument.keepConfidential")
                else ""
              )
            )
          )
      })

      Some(SummaryListRow(key = "uploadSupportingDocuments.checkYourAnswersLabel",
        value = ValueViewModel()))

    } else {
      None
    }

    Seq(
      descriptionRow,
      commodityCodeRow,
      legalChallengeRow,
      confidentialInformationRow,
      attachmentsRow
    ).flatten
  }
}
