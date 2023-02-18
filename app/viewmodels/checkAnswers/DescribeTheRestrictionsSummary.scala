package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.DescribeTheRestrictionsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DescribeTheRestrictionsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DescribeTheRestrictionsPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "describeTheRestrictions.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.DescribeTheRestrictionsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("describeTheRestrictions.change.hidden"))
          )
        )
    }
}
