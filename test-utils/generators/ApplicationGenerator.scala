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

package generators

import java.time.{LocalDate, ZoneOffset}

import models.requests._
import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary

trait ApplicationGenerator extends ApplicationRequestGenerator {

  implicit lazy val arbitraryApplication: Arbitrary[Application] =
    Arbitrary {
      for {
        id             <- applicationIdGen
        data           <- arbitraryApplicationRequest.arbitrary
        date           <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        dateInstant     = date.atStartOfDay(ZoneOffset.UTC).toInstant
        attachments    <- Gen.listOf(arbitrary[Attachment])
        whatIsYourRole <- Gen.oneOf(
                            WhatIsYourRoleResponse.AgentOrg,
                            WhatIsYourRoleResponse.EmployeeOrg,
                            WhatIsYourRoleResponse.AgentTrader
                          )
      } yield Application(
        id = id,
        lastUpdated = dateInstant,
        created = dateInstant,
        trader = data.trader,
        agent = data.agent,
        contact = data.contact,
        requestedMethod = data.requestedMethod,
        goodsDetails = data.goodsDetails,
        attachments = attachments,
        whatIsYourRoleResponse = Some(whatIsYourRole)
      )
    }

  implicit lazy val arbitraryAttachment: Arbitrary[Attachment] =
    Arbitrary {
      for {
        id          <- arbitrary[Long]
        name        <- arbitrary[String]
        description <- Gen.option(arbitrary[String])
        location    <- arbitrary[String]
        privacy     <- Gen.oneOf(Privacy.values)
        mimeType    <- arbitrary[String]
        size        <- arbitrary[Long]
      } yield Attachment(id, name, description, location, privacy, mimeType, size)
    }
}
