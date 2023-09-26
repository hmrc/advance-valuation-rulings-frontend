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

    val commodityCodeQuestionRow = Some(
      SummaryListRowViewModel(
        key = "hasCommodityCode.checkYourAnswersLabel",
        value = ValueViewModel(messages("site.yes"))
      )
    )

    val commodityCodeRow = goodsDetails.envisagedCommodityCode.map {
      commodityCode =>
        SummaryListRowViewModel(
          key = "commodityCode.checkYourAnswersLabel",
          value = ValueViewModel(commodityCode)
        )
    }

    val legalChallengesQuestionRow = Some(
      SummaryListRowViewModel(
        //LDS ignore
        key = "haveTheGoodsBeenSubjectToLegalChallenges.checkYourAnswersLabel",
        value = ValueViewModel(messages("site.yes"))
      )
    )

    val legalChallengeRow = goodsDetails.knownLegalProceedings.map {
      challenge =>
        SummaryListRowViewModel(
          key = "describeTheLegalChallenges.checkYourAnswersLabel",
          value = ValueViewModel(challenge)
        )
    }

    val confidentialInformationQuestionRow = Some(
      SummaryListRowViewModel(
        key = "hasConfidentialInformation.checkYourAnswersLabel",
        value = ValueViewModel(messages("site.yes"))
      )
    )

    val confidentialInformationRow: Option[SummaryListRow] =
      goodsDetails.confidentialInformation.map {
        info =>
          SummaryListRowViewModel(
            key = "confidentialInformation.checkYourAnswersLabel",
            value = ValueViewModel(info)
          )
      }

    val uploadSupportingDocumentsQuestionRow = Some(
      SummaryListRowViewModel(
        key = "doYouWantToUploadDocuments.checkYourAnswersLabel",
        value = ValueViewModel(messages("site.yes"))
      )
    )

    val attachmentRowContent = attachments
      .map(
        file =>
          s"${file.name} ${if (file.privacy == Confidential)
              s"<strong>- ${messages("uploadAnotherSupportingDocument.keepConfidential")}</strong>"
            else ""}"
      )
      .mkString("<br/>")

    val attachmentsRow = if (attachments.nonEmpty) {
      Some(
        SummaryListRowViewModel(
          key = "uploadSupportingDocuments.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(attachmentRowContent))
        )
      )
    } else {
      None
    }

    Seq(
      descriptionRow,
      commodityCodeQuestionRow,
      commodityCodeRow,
      legalChallengesQuestionRow,
      legalChallengeRow,
      confidentialInformationQuestionRow,
      confidentialInformationRow,
      uploadSupportingDocumentsQuestionRow,
      attachmentsRow
    ).flatten
  }
}
