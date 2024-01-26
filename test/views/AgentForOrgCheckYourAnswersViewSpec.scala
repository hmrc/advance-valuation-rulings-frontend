/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views

import models.DraftId
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.summary._
import views.behaviours.ViewBehaviours
import views.html.AgentForOrgCheckYourAnswersView

class AgentForOrgCheckYourAnswersViewSpec extends ViewBehaviours with BaseSelectors {

  val messageKeyPrefix = "checkYourAnswersForAgents"

  val testEoriDetailsSummary: IndividualEoriDetailsSummary = IndividualEoriDetailsSummary(SummaryList())
  val testApplicantSummary: IndividualApplicantSummary     = IndividualApplicantSummary(SummaryList())
  val testDetailsSummary: DetailsSummary                   = DetailsSummary(SummaryList())
  val testMethodSummary: MethodSummary                     = MethodSummary(SummaryList())

  // Use the instances to create an instance of ApplicationSummary
  val testApplicationSummary: ApplicationSummary =
    ApplicationSummary(testEoriDetailsSummary, testApplicantSummary, testDetailsSummary, testMethodSummary)

  val view: AgentForOrgCheckYourAnswersView =
    app.injector.instanceOf[AgentForOrgCheckYourAnswersView]

  val viewViaApply: HtmlFormat.Appendable  =
    view(testApplicationSummary, DraftId(1L))(fakeRequest, messages)
  val viewViaRender: HtmlFormat.Appendable =
    view.render(testApplicationSummary, DraftId(1L), fakeRequest, messages)
  val viewViaF: HtmlFormat.Appendable      =
    view.f(testApplicationSummary, DraftId(1L))(fakeRequest, messages)

  "AgentForOrgCheckYourAnswersView" - {
    normalPage(messageKeyPrefix, "")()
  }

  "should have correct additional content" - {

    val expectedContent =
      Seq(
        h2(1)        -> "About the company you are representing",
        //summarylist
        h2(2)        -> "About the agent",
        //summarylist
        h2(3)        -> "About the goods",
        //summarylist
        //summarylist
        h2(4)        -> "Your declaration",
        p(1)         -> "By applying for an Advance Valuation Ruling you confirm that:",
        bullet(1)    -> "you have selected the most relevant method to value the company’s goods",
        bullet(2)    -> "the information you have provided is correct and complete to the best of your knowledge",
        bullet(3)    -> "your application is based on an intention to import goods into Great Britain",
        bullet(
          4
        )            -> "you are aware that it can take up to 120 days from the date your application is accepted to receive an Advance Valuation Ruling decision",
        bullet(
          5
        )            -> "you will not need to apply for an Advance Valuation Ruling every time you import these goods via the same valuation method, since the ruling is legally binding for three years from the date of issue",
        p(2)         -> "You agree that HMRC can:",
        bullet(1, 2) -> "dispute the valuation method you have selected",
        bullet(2, 2) -> "store the information you provided (or may provide) after you send your application",
        bullet(3, 2) -> "contact you by email"
      )

    behave like pageWithExpectedMessages(viewViaApply, expectedContent)
  }
}
