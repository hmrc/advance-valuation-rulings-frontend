package models

import play.api.libs.json.{JsError, Json, JsString}

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class RequiredInformationSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with ModelGenerators {

  "RequiredInformation" - {

    "must deserialise valid values" in {

      val gen = arbitrary[RequiredInformation]

      forAll(gen) {
        requiredInformation =>
          JsString(requiredInformation.toString)
            .validate[RequiredInformation]
            .asOpt
            .value mustEqual requiredInformation
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!RequiredInformation.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[RequiredInformation] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = arbitrary[RequiredInformation]

      forAll(gen) {
        requiredInformation =>
          Json.toJson(requiredInformation) mustEqual JsString(requiredInformation.toString)
      }
    }
  }
}
