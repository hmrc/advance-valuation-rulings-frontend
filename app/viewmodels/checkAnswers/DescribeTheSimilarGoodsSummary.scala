package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.DescribeTheSimilarGoodsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DescribeTheSimilarGoodsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DescribeTheSimilarGoodsPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "describeTheSimilarGoods.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.DescribeTheSimilarGoodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("describeTheSimilarGoods.change.hidden"))
          )
        )
    }
}
