package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ExplainWhyYouHaveNotSeletedMethodOneToFivePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ExplainWhyYouHaveNotSeletedMethodOneToFiveSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ExplainWhyYouHaveNotSeletedMethodOneToFivePage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "explainWhyYouHaveNotSeletedMethodOneToFive.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ExplainWhyYouHaveNotSeletedMethodOneToFiveController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("explainWhyYouHaveNotSeletedMethodOneToFive.change.hidden"))
          )
        )
    }
}
