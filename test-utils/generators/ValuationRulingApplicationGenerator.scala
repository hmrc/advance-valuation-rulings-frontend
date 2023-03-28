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

trait ApplicationGenerator extends ApplicationRequestGenerator {

  implicit lazy val arbitraryApplication: Arbitrary[Application] =
    Arbitrary {
      for {
        id        <- applicationIdGen
        data      <- arbitraryApplicationRequest.arbitrary
        date      <- datesBetween(LocalDate.of(2000, 1, 1), LocalDate.of(3000, 1, 1))
        dateIntant = date.atStartOfDay(ZoneOffset.UTC).toInstant
      } yield Application(
        id = id,
        request = data,
        lastUpdated = dateIntant,
        created = dateIntant
      )
    }
}
