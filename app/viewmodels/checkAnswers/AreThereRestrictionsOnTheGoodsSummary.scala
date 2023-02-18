package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.AreThereRestrictionsOnTheGoodsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AreThereRestrictionsOnTheGoodsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AreThereRestrictionsOnTheGoodsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "areThereRestrictionsOnTheGoods.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AreThereRestrictionsOnTheGoodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("areThereRestrictionsOnTheGoods.change.hidden"))
          )
        )
    }
}
