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

package viewmodels.checkAnswers.summary

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import models.UserAnswers
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._

case class DetailsSummary(rows: SummaryList) extends AnyVal

object DetailsSummary {
  def apply(userAnswers: UserAnswers)(implicit
    messages: Messages
  ): DetailsSummary = {

    val rows = Seq(
      DescriptionOfGoodsSummary.row(userAnswers),
      HaveYouReceivedADecisionSummary.row(userAnswers),
      TellUsAboutYourRulingSummary.row(userAnswers),
      AwareOfRulingSummary.row(userAnswers),
      AboutSimilarGoodsSummary.row(userAnswers),
      HasCommodityCodeSummary.row(userAnswers),
      CommodityCodeSummary.row(userAnswers),
      HaveTheGoodsBeenSubjectToLegalChallengesSummary.row(userAnswers),
      DescribeTheLegalChallengesSummary.row(userAnswers),
      HasConfidentialInformationSummary.row(userAnswers),
      ConfidentialInformationSummary.row(userAnswers),
      DoYouWantToUploadDocumentsSummary.row(userAnswers),
      UploadedDocumentsSummary.row(userAnswers)
    ).flatten

    DetailsSummary(SummaryListViewModel(rows))
  }
}
