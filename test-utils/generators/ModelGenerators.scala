package generators

import models._
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

trait ModelGenerators {

  implicit lazy val arbitraryRequiredInformation: Arbitrary[RequiredInformation] =
    Arbitrary {
      Gen.oneOf(RequiredInformation.values)
    }

  implicit lazy val arbitraryValuationMethod: Arbitrary[ValuationMethod] =
    Arbitrary {
      Gen.oneOf(ValuationMethod.values.toSeq)
    }
}
