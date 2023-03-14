package models

import play.api.libs.json.{JsError, Json, JsString}

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class WhatIsYourRoleAsImporterSpec
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues {

  "WhatIsYourRoleAsImporter" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(WhatIsYourRoleAsImporter.values.toSeq)

      forAll(gen) {
        whatIsYourRoleAsImporter =>
          JsString(whatIsYourRoleAsImporter.toString)
            .validate[WhatIsYourRoleAsImporter]
            .asOpt
            .value mustEqual whatIsYourRoleAsImporter
      }
    }

    "must fail to deserialise invalid values" in {

      val gen =
        arbitrary[String] suchThat (!WhatIsYourRoleAsImporter.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[WhatIsYourRoleAsImporter] mustEqual JsError(
            "error.invalid"
          )
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(WhatIsYourRoleAsImporter.values.toSeq)

      forAll(gen) {
        whatIsYourRoleAsImporter =>
          Json.toJson(whatIsYourRoleAsImporter) mustEqual JsString(
            whatIsYourRoleAsImporter.toString
          )
      }
    }
  }
}
