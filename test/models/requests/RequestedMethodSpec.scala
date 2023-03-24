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

package models.requests

import cats.data.NonEmptyList
import cats.data.Validated._

import generators._
import models._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class RequestedMethodSpec
    extends AnyWordSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with ApplicationRequestGenerator {

  import RequestedMethodSpec._

  "ApplicationRequest" should {
    // "succeed for an individual applicant" in {
    //   val ua = UserAnswers("a", applicationNumber)

    //   val userAnswers = (for {
    //     ua <- ua.set(CheckRegisteredDetailsPage, checkRegisteredDetails)
    //     ua <- ua.set(ApplicationContactDetailsPage, applicationContactDetails)
    //   } yield ua).success.get

    //   val result = Applicant(userAnswers)

    //   result shouldBe Valid(IndividualApplicant(applicant.holder, applicant.contact))
    // }

    // "succeed for a business applicant" in {
    //   val ua = UserAnswers("a", applicationNumber)

    //   val userAnswers = (for {
    //     ua <- ua.set(CheckRegisteredDetailsPage, checkRegisteredDetails)
    //     ua <- ua.set(BusinessContactDetailsPage, businessContactDetails)
    //   } yield ua).success.get

    //   val result = Applicant(userAnswers)

    //   result shouldBe Valid(
    //     OrganisationApplicant(orgApplicant.holder, orgApplicant.businessContact)
    //   )
    // }

    "return valid for method four with all answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, randomString)
        ua <- ua.set(ExplainWhyYouChoseMethodFourPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodFour(
          randomString,
          randomString
        )
      )
    }

    "return invalid for method four without ExplainWhyYouHaveNotSelectedMethodOneToThreePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
        ua <- ua.set(ExplainWhyYouChoseMethodFourPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainWhyYouHaveNotSelectedMethodOneToThreePage)
      )
    }

    "return invalid for method four without ExplainWhyYouChoseMethodFourPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToThreePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainWhyYouChoseMethodFourPage)
      )
    }

    "return invalid for method four with only ValuationMethodPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method4)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          ExplainWhyYouHaveNotSelectedMethodOneToThreePage,
          List(ExplainWhyYouChoseMethodFourPage)
        )
      )
    }

    "return valid for method five with all answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
        ua <- ua.set(WhyComputedValuePage, randomString)
        ua <- ua.set(ExplainReasonComputedValuePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodFive(
          randomString,
          randomString
        )
      )
    }

    "return invalid for method five without WhyComputedValuePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
        ua <- ua.set(ExplainReasonComputedValuePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(WhyComputedValuePage)
      )
    }

    "return invalid for method five without ExplainReasonComputedValuePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
        ua <- ua.set(WhyComputedValuePage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainReasonComputedValuePage)
      )
    }

    "return invalid for method five with only ValuationMethodPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method5)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          WhyComputedValuePage,
          List(ExplainReasonComputedValuePage)
        )
      )
    }

    "return valid for method six with all answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, randomString)
        ua <- ua.set(AdaptMethodPage, AdaptMethod.Method5)
        ua <- ua.set(ExplainHowYouWillUseMethodSixPage, randomString)

      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Valid(
        MethodSix(
          randomString,
          AdaptedMethod.MethodFive,
          randomString
        )
      )
    }

    "return invalid for method six without ExplainWhyYouHaveNotSelectedMethodOneToFivePage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(AdaptMethodPage, AdaptMethod.Method5)
        ua <- ua.set(ExplainHowYouWillUseMethodSixPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainWhyYouHaveNotSelectedMethodOneToFivePage)
      )
    }

    "return invalid for method six without AdaptMethodPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, randomString)
        ua <- ua.set(ExplainHowYouWillUseMethodSixPage, randomString)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(AdaptMethodPage)
      )
    }

    "return invalid for method six without ExplainHowYouWillUseMethodSixPage" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
        ua <- ua.set(ExplainWhyYouHaveNotSelectedMethodOneToFivePage, randomString)
        ua <- ua.set(AdaptMethodPage, AdaptMethod.Method5)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ExplainHowYouWillUseMethodSixPage)
      )
    }

    "return invalid for method six without answers" in {
      val userAnswers = (for {
        ua <- emptyUserAnswers.set(ValuationMethodPage, ValuationMethod.Method6)
      } yield ua).success.get

      val result = RequestedMethod(userAnswers)

      result shouldBe Invalid(
        NonEmptyList(
          ExplainWhyYouHaveNotSelectedMethodOneToFivePage,
          List(AdaptMethodPage, ExplainHowYouWillUseMethodSixPage)
        )
      )
    }

    "return invalid for empty UserAnswers" in {
      val result = RequestedMethod(emptyUserAnswers)

      result shouldBe Invalid(
        NonEmptyList.one(ValuationMethodPage)
      )
    }
  }
}

object RequestedMethodSpec extends Generators {
  val randomString: String = stringsWithMaxLength(8).sample.get

  val checkRegisteredDetails        = CheckRegisteredDetails(
    value = true,
    eori = randomString,
    name = randomString,
    streetAndNumber = randomString,
    city = randomString,
    country = randomString,
    postalCode = Some(randomString)
  )
  val applicationNumber: String     = ApplicationNumber("GBAVR", 1).render
  val emptyUserAnswers: UserAnswers = UserAnswers("id", applicationNumber)

  val applicationContactDetails = ApplicationContactDetails(
    name = randomString,
    email = randomString,
    phone = randomString
  )
  val businessContactDetails    = BusinessContactDetails(
    name = randomString,
    email = randomString,
    phone = randomString,
    company = randomString
  )
  val applicant                 = IndividualApplicant(
    holder = EORIDetails(
      eori = randomString,
      businessName = randomString,
      addressLine1 = randomString,
      addressLine2 = "",
      addressLine3 = randomString,
      postcode = randomString,
      country = randomString
    ),
    contact = ContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString)
    )
  )
  val orgApplicant              = OrganisationApplicant(
    holder = EORIDetails(
      eori = randomString,
      businessName = randomString,
      addressLine1 = randomString,
      addressLine2 = "",
      addressLine3 = randomString,
      postcode = randomString,
      country = randomString
    ),
    businessContact = CompanyContactDetails(
      name = randomString,
      email = randomString,
      phone = Some(randomString),
      company = randomString
    )
  )
}
