package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class AdaptMethodSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "AdaptMethod" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(AdaptMethod.values.toSeq)

      forAll(gen) {
        adaptMethod =>

          JsString(adaptMethod.toString).validate[AdaptMethod].asOpt.value mustEqual adaptMethod
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AdaptMethod.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AdaptMethod] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(AdaptMethod.values.toSeq)

      forAll(gen) {
        adaptMethod =>

          Json.toJson(adaptMethod) mustEqual JsString(adaptMethod.toString)
      }
    }
  }
}
