/*
 * Copyright 2025 HM Revenue & Customs
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

package services.checkAnswers

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import userrole.{UserRole, UserRoleProvider}
import viewmodels.checkAnswers.summary._

class ApplicationSummaryServiceSpec extends SpecBase {

  private implicit val messages: Messages = stubMessages()

  private val mockUserRoleProvider: UserRoleProvider = mock(classOf[UserRoleProvider])
  private val mockUserRole: UserRole                 = mock(classOf[UserRole])

  ".getApplicationSummary" - {
    "must return the expected ApplicationSummary" in {
      when(mockUserRoleProvider.getUserRole(any())).thenReturn(mockUserRole)
      when(mockUserRole.getApplicationSummary(any(), any())(any()))
        .thenReturn((testApplicantSummary, testEoriDetailsSummary))

      val service: ApplicationSummaryService = new ApplicationSummaryService(mockUserRoleProvider)

      val result: ApplicationSummary =
        service.getApplicationSummary(userAnswersAsIndividualTrader, traderDetailsWithCountryCode)

      result mustBe ApplicationSummary(
        eoriDetails = testEoriDetailsSummary,
        applicant = testApplicantSummary,
        details = DetailsSummary(userAnswersAsIndividualTrader),
        method = MethodSummary(userAnswersAsIndividualTrader)
      )
    }
  }
}
