package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.DescribeTheConditionsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DescribeTheConditionsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DescribeTheConditionsPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "describeTheConditions.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.DescribeTheConditionsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("describeTheConditions.change.hidden"))
          )
        )
    }
}
