package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ValuationMethodSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ValuationMethod" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ValuationMethod.values.toSeq)

      forAll(gen) {
        valuationMethod =>

          JsString(valuationMethod.toString).validate[ValuationMethod].asOpt.value mustEqual valuationMethod
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ValuationMethod.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ValuationMethod] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ValuationMethod.values.toSeq)

      forAll(gen) {
        valuationMethod =>

          Json.toJson(valuationMethod) mustEqual JsString(valuationMethod.toString)
      }
    }
  }
}
