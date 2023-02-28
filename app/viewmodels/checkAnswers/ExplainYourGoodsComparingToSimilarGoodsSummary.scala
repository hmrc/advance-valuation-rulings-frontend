package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ExplainYourGoodsComparingToSimilarGoodsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ExplainYourGoodsComparingToSimilarGoodsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ExplainYourGoodsComparingToSimilarGoodsPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "explainYourGoodsComparingToSimilarGoods.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ExplainYourGoodsComparingToSimilarGoodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("explainYourGoodsComparingToSimilarGoods.change.hidden"))
          )
        )
    }
}
