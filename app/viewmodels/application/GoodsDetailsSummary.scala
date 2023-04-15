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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import models.requests.{AttachmentRequest, GoodsDetails}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object GoodsDetailsSummary {

  def rows(goodsDetails: GoodsDetails, attachments: Seq[AttachmentRequest])(implicit
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

    val confidentialInformationRow = goodsDetails.confidentialInformation.map {
      info =>
        SummaryListRowViewModel(
          key = "confidentialInformation.checkYourAnswersLabel",
          value = ValueViewModel(info)
        )
    }

    val attachmentsRow = if (attachments.nonEmpty) {
      Some(
        SummaryListRowViewModel(
          key = "uploadSupportingDocuments.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(attachments.map(_.name).mkString("<br/>")))
        )
      )
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
