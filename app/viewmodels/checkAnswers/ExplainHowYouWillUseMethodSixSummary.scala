package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.ExplainHowYouWillUseMethodSixPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object ExplainHowYouWillUseMethodSixSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(ExplainHowYouWillUseMethodSixPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "explainHowYouWillUseMethodSix.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ExplainHowYouWillUseMethodSixController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("explainHowYouWillUseMethodSix.change.hidden"))
          )
        )
    }
}
