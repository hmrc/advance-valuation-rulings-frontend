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

package models

import base.SpecBase
import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class RequiredInformationSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  "RequiredInformation" - {

    "must deserialise valid values" in {

      val gen = arbitrary[RequiredInformation]

      forAll(gen) { requiredInformation =>
        JsString(requiredInformation.toString)
          .validate[RequiredInformation]
          .asOpt
          .value mustEqual requiredInformation
      }
    }

    "must display individual checkbox items" in {

      implicit val m: Messages = play.api.test.Helpers.stubMessages()

      val result  = RequiredInformation.checkboxItems(AuthUserType.IndividualTrader)(m)
      val content = result.map(_.content)
      content must contain(Text("requiredInformation.option1.individual"))
      content must contain(Text("requiredInformation.option2.individual"))
      content must contain(Text("requiredInformation.option3.individual"))
      content must contain(Text("requiredInformation.option4.individual"))
      content must contain(Text("requiredInformation.option5.individual"))
      content must contain(Text("requiredInformation.option6.individual"))
    }

    "must display organisation checkbox items" in {

      implicit val m: Messages = play.api.test.Helpers.stubMessages()

      val result  = RequiredInformation.checkboxItems(AuthUserType.OrganisationUser)(m)
      val content = result.map(_.content)
      content must contain(Text("requiredInformation.option1.organisation"))
      content must contain(Text("requiredInformation.option2.organisation"))
      content must contain(Text("requiredInformation.option3.organisation"))
      content must contain(Text("requiredInformation.option4.organisation"))
      content must contain(Text("requiredInformation.option5.organisation"))
      content must contain(Text("requiredInformation.option6.organisation"))
    }

    "must display agent checkbox items" in {

      implicit val m: Messages = play.api.test.Helpers.stubMessages()

      val result  = RequiredInformation.checkboxItems(AuthUserType.Agent)(m)
      val content = result.map(_.content)
      content must contain(Text("requiredInformation.option1.organisation"))
      content must contain(Text("requiredInformation.option2.organisation"))
      content must contain(Text("requiredInformation.option3.organisation"))
      content must contain(Text("requiredInformation.option4.organisation"))
      content must contain(Text("requiredInformation.option5.organisation"))
      content must contain(Text("requiredInformation.option6.organisation"))
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!RequiredInformation.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>
        JsString(invalidValue).validate[RequiredInformation] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[RequiredInformation]

      forAll(gen) { requiredInformation =>
        Json.toJson(requiredInformation) mustEqual JsString(requiredInformation.toString)
      }
    }
  }
}
