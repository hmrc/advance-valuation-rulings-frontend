package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.DescribeTheLegalChallengesPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DescribeTheLegalChallengesSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DescribeTheLegalChallengesPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "describeTheLegalChallenges.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.DescribeTheLegalChallengesController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("describeTheLegalChallenges.change.hidden"))
          )
        )
    }
}
