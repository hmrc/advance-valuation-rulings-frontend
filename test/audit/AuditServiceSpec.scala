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

package audit

import base.SpecBase
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, AgentOnBehalfOfTrader, EmployeeOfOrg}
import models.events.{RoleIndicatorEvent, UserTypeEvent}
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.{mock, reset, times, verify}
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{AffinityGroup, User}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends SpecBase with TableDrivenPropertyChecks {

  private val mockAuditConnector = mock[AuditConnector]
  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "sendUserTypeEvent" - {

    implicit val identifierRequest: IdentifierRequest[_] = IdentifierRequest(
      request = FakeRequest().withHeaders("Referer" -> "some referer"),
      userId = userAnswersId,
      eoriNumber = EoriNumber,
      affinityGroup = AffinityGroup.Individual,
      credentialRole = Option(User)
    )

    val event = UserTypeEvent(
      identifierRequest.userId,
      identifierRequest.eoriNumber,
      identifierRequest.affinityGroup,
      identifierRequest.credentialRole,
      Some("some referer")
    )

    "must send userTypeEvent to auditConnector" in {
      new AuditService(mockAuditConnector).sendUserTypeEvent()

      verify(mockAuditConnector, times(1))
        .sendExplicitAudit(eqTo("UserEntersService"), eqTo(event))(any(), any(), any())
    }
  }

  "sendAgentIndicatorEvent" - {

    implicit val dataRequest: DataRequest[_] = DataRequest(
      request = FakeRequest(),
      userId = userAnswersId,
      eoriNumber = EoriNumber,
      affinityGroup = AffinityGroup.Individual,
      credentialRole = Option(User),
      userAnswers = userAnswersAsIndividualTrader
    )

    val importerRoles = Seq(
      EmployeeOfOrg,
      AgentOnBehalfOfOrg,
      AgentOnBehalfOfTrader
    )

    "must send AgentIndicatorEvent to auditConnector" in {
      importerRoles.foreach { importerRole =>
        val event = RoleIndicatorEvent(
          dataRequest.userId,
          dataRequest.eoriNumber,
          dataRequest.affinityGroup,
          dataRequest.credentialRole,
          importerRole
        )
        new AuditService(mockAuditConnector).sendRoleIndicatorEvent(importerRole)

        verify(mockAuditConnector, times(1))
          .sendExplicitAudit(eqTo("IndicatesRole"), eqTo(event))(any(), any(), any())
      }
    }
  }
}
