package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.WillYouCompareToSimilarGoodsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WillYouCompareToSimilarGoodsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WillYouCompareToSimilarGoodsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "willYouCompareToSimilarGoods.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.WillYouCompareToSimilarGoodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("willYouCompareToSimilarGoods.change.hidden"))
          )
        )
    }
}
