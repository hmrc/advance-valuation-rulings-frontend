package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.HaveYouUsedMethodOneForSimilarGoodsInPastPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object HaveYouUsedMethodOneForSimilarGoodsInPastSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HaveYouUsedMethodOneForSimilarGoodsInPastPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "haveYouUsedMethodOneForSimilarGoodsInPast.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.HaveYouUsedMethodOneForSimilarGoodsInPastController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("haveYouUsedMethodOneForSimilarGoodsInPast.change.hidden"))
          )
        )
    }
}
