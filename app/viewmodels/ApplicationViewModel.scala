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

package viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import models.requests._
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.summary._

case class ApplicationViewModel(
  eori: SummaryList,
  applicant: SummaryList,
  details: DetailsSummary,
  method: MethodSummary
)

object ApplicationViewModel {
  def apply(application: Application)(implicit
    messages: Messages
  ): ApplicationViewModel = {
    val applicationRequest = application.request
    val eoriRow            = CheckRegisteredDetailsSummary.rows(applicationRequest).map(_.copy(actions = None))
    val applicant          =
      ApplicationContactDetailsSummary.rows(applicationRequest.contact).map(_.copy(actions = None))

    ApplicationViewModel(
      eori = SummaryList(eoriRow),
      applicant = SummaryList(applicant),
      details = DetailsSummary(applicationRequest).removeActions(),
      method = MethodSummary(applicationRequest).removeActions()
    )
  }
}
