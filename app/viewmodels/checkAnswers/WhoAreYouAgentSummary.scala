package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.WhoAreYouAgentPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhoAreYouAgentSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WhoAreYouAgentPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "whoAreYouAgent.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.WhoAreYouAgentController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("whoAreYouAgent.change.hidden"))
          )
        )
    }
}
