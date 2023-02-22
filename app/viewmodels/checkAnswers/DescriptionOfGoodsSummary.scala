package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.DescriptionOfGoodsPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DescriptionOfGoodsSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DescriptionOfGoodsPage).map {
      answer =>

        SummaryListRowViewModel(
          key     = "descriptionOfGoods.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.DescriptionOfGoodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("descriptionOfGoods.change.hidden"))
          )
        )
    }
}
