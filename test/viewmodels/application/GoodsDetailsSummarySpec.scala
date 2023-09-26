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
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Key, Value}

import models.requests.{Attachment, GoodsDetails, Privacy}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class GoodsDetailsSummarySpec extends AnyFreeSpec with Matchers {

  private implicit val m: Messages = stubMessages()
  private val goods                =
    GoodsDetails(
      "name",
      "description",
      Some("commodity code"),
      Some("legal proceedings"),
      Some("confidential info")
    )

  private val attachments: Seq[Attachment]                     =
    Seq(
      Attachment(1L, "name 1", None, "url 1", Privacy.Public, "mime", 1L),
      Attachment(2L, "name 2", None, "url 2", Privacy.Public, "mime", 2L)
    )
  private val attachmentsWithConfidentialElem: Seq[Attachment] =
    Seq(
      Attachment(1L, "name 1", None, "url 1", Privacy.Confidential, "mime", 1L),
      Attachment(2L, "name 2", None, "url 2", Privacy.Public, "mime", 2L)
    )

  ".rows" - {

    "must contain only description when optional values are missing" in {

      val goods = GoodsDetails(
        "name",
        "description",
        None,
        None,
        None
      )

      GoodsDetailsSummary.rows(goods, Nil) must contain theSameElementsInOrderAs Seq(
        SummaryListRow(
          Key(Text(m("descriptionOfGoods.checkYourAnswersLabel"))),
          Value(Text(goods.goodsName))
        ),
        SummaryListRow(
          Key(Text(m("hasCommodityCode.checkYourAnswersLabel"))),
          Value(Text("site.no"))
        ),
        SummaryListRow(
          // LDS ignore
          Key(Text(m("haveTheGoodsBeenSubjectToLegalChallenges.checkYourAnswersLabel"))),
          Value(Text("site.no"))
        ),
        SummaryListRow(
          Key(Text(m("hasConfidentialInformation.checkYourAnswersLabel"))),
          Value(Text("site.no"))
        ),
        SummaryListRow(
          Key(Text(m("doYouWantToUploadDocuments.checkYourAnswersLabel"))),
          Value(Text("site.no"))
        )
      )
    }

    "must contain rows for all fields when optional values are present" in {

      GoodsDetailsSummary.rows(goods, attachments) must contain theSameElementsInOrderAs Seq(
        SummaryListRow(
          Key(Text(m("descriptionOfGoods.checkYourAnswersLabel"))),
          Value(Text(goods.goodsName))
        ),
        SummaryListRow(
          Key(Text(m("hasCommodityCode.checkYourAnswersLabel"))),
          Value(Text("site.yes"))
        ),
        SummaryListRow(
          Key(Text(m("commodityCode.checkYourAnswersLabel"))),
          Value(Text(goods.envisagedCommodityCode.get))
        ),
        SummaryListRow(
          // LDS ignore
          Key(Text(m("haveTheGoodsBeenSubjectToLegalChallenges.checkYourAnswersLabel"))),
          Value(Text("site.yes"))
        ),
        SummaryListRow(
          Key(Text(m("describeTheLegalChallenges.checkYourAnswersLabel"))),
          Value(Text(goods.knownLegalProceedings.get))
        ),
        SummaryListRow(
          Key(Text(m("hasConfidentialInformation.checkYourAnswersLabel"))),
          Value(Text("site.yes"))
        ),
        SummaryListRow(
          Key(Text(m("confidentialInformation.checkYourAnswersLabel"))),
          Value(Text(goods.confidentialInformation.get))
        ),
        SummaryListRow(
          Key(Text(m("doYouWantToUploadDocuments.checkYourAnswersLabel"))),
          Value(Text("site.yes"))
        ),
        SummaryListRow(
          Key(Text(m("uploadSupportingDocuments.checkYourAnswersLabel"))),
          Value(
            HtmlContent(
              "name 1 <br/>name 2 "
            )
          )
        )
      )
    }

    "Attachments row" - {
      "must suffix confidential files with 'keep confidential'" in {
        GoodsDetailsSummary.rows(
          goods,
          attachmentsWithConfidentialElem
        ) must contain atLeastOneElementOf Seq(
          SummaryListRow(
            Key(Text(m("uploadSupportingDocuments.checkYourAnswersLabel"))),
            Value(
              HtmlContent(
                "name 1 <strong>- uploadAnotherSupportingDocument.keepConfidential</strong><br/>name 2 "
              )
            )
          )
        )
      }
    }
  }
}
