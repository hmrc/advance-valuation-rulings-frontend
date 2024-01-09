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

import models.WhatIsYourRoleAsImporter
import models.events.{RoleIndicatorEvent, UserTypeEvent}
import models.requests.{DataRequest, IdentifierRequest}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: AuditConnector) {

  def sendUserTypeEvent()(implicit
    identifierRequest: IdentifierRequest[_],
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Unit = {
    import identifierRequest._

    val referrer = identifierRequest.headers.get("Referer")
    val detail   = UserTypeEvent(userId, eoriNumber, affinityGroup, credentialRole, referrer)
    auditConnector.sendExplicitAudit("UserEntersService", detail)
  }

  def sendRoleIndicatorEvent(
    role: WhatIsYourRoleAsImporter
  )(implicit dataRequest: DataRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    import dataRequest._

    val detail =
      RoleIndicatorEvent(userId, eoriNumber, affinityGroup, credentialRole, role)
    auditConnector.sendExplicitAudit("IndicatesRole", detail)
  }

}
