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

package audit

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{AffinityGroup, User}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import base.SpecBase
import models.WhatIsYourRoleAsImporter.{AgentOnBehalfOfOrg, EmployeeOfOrg}
import models.events.{AgentIndicatorEvent, UserTypeEvent}
import models.requests.{DataRequest, IdentifierRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.reset
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar

class AuditServiceSpec extends SpecBase with TableDrivenPropertyChecks with MockitoSugar {

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

    val importerRoles = Table(
      ("importerRole", "isAgent"),
      (EmployeeOfOrg, false),
      (AgentOnBehalfOfOrg, true)
    )

    "must send AgentIndicatorEvent to auditConnector" in {
      forAll(importerRoles) {
        case (importerRole, isAgent) =>
          val event = AgentIndicatorEvent(
            dataRequest.userId,
            dataRequest.eoriNumber,
            dataRequest.affinityGroup,
            dataRequest.credentialRole,
            Option(isAgent)
          )

          new AuditService(mockAuditConnector).sendAgentIndicatorEvent(importerRole)

          verify(mockAuditConnector, times(1))
            .sendExplicitAudit(eqTo("IndicatesIsAgent"), eqTo(event))(any(), any(), any())
      }
    }
  }
}
