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

package controllers.actions

import base.SpecBase
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class IdentifyEoriSpec extends SpecBase {

  "IdentifyEori object" - {
    "getEoriNumber method" - {
      "should return the correct EORI number" in {
        val enrolmentId  = "EORINumber"
        val enrolmentKey = "HMRC-ATAR-ORG"
        val eoriNumber   = "GB1234567890"
        val identifier   = EnrolmentIdentifier(enrolmentId, eoriNumber)
        val enrolment    = Enrolment(enrolmentKey, Seq(identifier), "Activated", None)
        val enrolments   = Enrolments(Set(enrolment))

        val result = IdentifyEori.getEoriNumber(enrolments)

        result mustBe Some(eoriNumber)
      }

      "should return None when the Enrolments object does not contain the HMRC-ATAR-ORG enrolment" in {
        val enrolments = Enrolments(Set.empty)

        val result = IdentifyEori.getEoriNumber(enrolments)

        result mustBe None
      }

      "should return None when the HMRC-ATAR-ORG enrolment does not contain the EORINumber identifier" in {
        val enrolmentKey = "HMRC-ATAR-ORG"
        val enrolment    = Enrolment(enrolmentKey, Seq.empty, "Activated", None)
        val enrolments   = Enrolments(Set(enrolment))

        val result = IdentifyEori.getEoriNumber(enrolments)

        result mustBe None
      }

      "should return None when the Enrolments object is empty" in {
        val enrolments = Enrolments(Set.empty)

        val result = IdentifyEori.getEoriNumber(enrolments)

        result mustBe None
      }
    }
  }
}
