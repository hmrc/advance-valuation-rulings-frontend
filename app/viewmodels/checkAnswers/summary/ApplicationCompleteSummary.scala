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

package viewmodels.checkAnswers.summary

import play.api.Logger
import play.api.i18n.Messages
import uk.gov.hmrc.auth.core.{AffinityGroup, UnsupportedAffinityGroup}

import models.requests.Application

case class ApplicationCompleteSummary(
  eoriDetails: EoriDetailsSummary,
  applicant: ApplicantSummary,
  details: DetailsSummary,
  method: MethodSummary
) {

  def removeActions(): ApplicationCompleteSummary =
    copy(
      eoriDetails = eoriDetails.removeActions(),
      applicant = applicant.removeActions(),
      details = details.removeActions(),
      method = method.removeActions()
    )
}

object ApplicationCompleteSummary {

  private val logger = Logger(this.getClass)

  def apply(application: Application, affinityGroup: AffinityGroup)(implicit
    messages: Messages
  ): ApplicationCompleteSummary = {
    val (applicant, company) = affinityGroup match {
      case AffinityGroup.Individual                         =>
        (IndividualApplicantSummary(application), IndividualEoriDetailsSummary(application))
      case AffinityGroup.Organisation | AffinityGroup.Agent =>
        (AgentSummary(application), BusinessEoriDetailsSummary(application))
      case unexpected                                       =>
        logger.error(s"Unsupported affinity group [$unexpected] encountered")
        throw UnsupportedAffinityGroup("Unexpected affinity group")
    }

    ApplicationCompleteSummary(
      eoriDetails = company,
      applicant = applicant,
      details = DetailsSummary(application),
      method = MethodSummary(application)
    ).removeActions()
  }
}
