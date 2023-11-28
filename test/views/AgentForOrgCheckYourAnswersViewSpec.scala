/*
 * Copyright 2023 HM Revenue & Customs
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

class AgentForOrgCheckYourAnswersViewSpec extends ViewBehaviours {

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

  val viewViaApply: () => HtmlFormat.Appendable  =
    () => view(testApplicationSummary, DraftId(1L))(fakeRequest, messages)
  val viewViaRender: () => HtmlFormat.Appendable =
    () => view.render(testApplicationSummary, DraftId(1L), fakeRequest, messages)
  val viewViaF: () => HtmlFormat.Appendable      =
    () => view.f(testApplicationSummary, DraftId(1L))(fakeRequest, messages)

  "AcceptItemInformationList view" - {
    def test(method: String, view: () => HtmlFormat.Appendable): Unit =
      s"$method" - {
        behave like normalPage(view, messageKeyPrefix, "")()
      }

    val input: Seq[(String, () => HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => (test _).tupled(args))
  }
}
