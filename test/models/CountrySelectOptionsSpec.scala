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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class CountrySelectOptionsSpec extends AnyFreeSpec with Matchers {

  "countryCodeToCountry" - {
    "must return the country for a code" in {
      CountrySelectOptions.countryCodeToCountry("GB") mustEqual "United Kingdom"
    }

    "must return the country for a terrority code" in {
      CountrySelectOptions.countryCodeToCountry("IO") mustEqual "British Indian Ocean Territory"
    }

    "must throw an IllegalArgumentException when no country exists" in {
      intercept[IllegalArgumentException] {
        CountrySelectOptions.countryCodeToCountry("XX")
      }
    }
  }
}
