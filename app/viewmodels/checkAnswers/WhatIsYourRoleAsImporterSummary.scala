package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.WhatIsYourRoleAsImporterPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WhatIsYourRoleAsImporterSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(WhatIsYourRoleAsImporterPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"whatIsYourRoleAsImporter.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = "whatIsYourRoleAsImporter.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.WhatIsYourRoleAsImporterController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("whatIsYourRoleAsImporter.change.hidden"))
          )
        )
    }
}
