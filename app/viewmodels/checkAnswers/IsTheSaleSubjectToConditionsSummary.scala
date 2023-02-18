package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.IsTheSaleSubjectToConditionsPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object IsTheSaleSubjectToConditionsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(IsTheSaleSubjectToConditionsPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = "isTheSaleSubjectToConditions.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.IsTheSaleSubjectToConditionsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("isTheSaleSubjectToConditions.change.hidden"))
          )
        )
    }
}