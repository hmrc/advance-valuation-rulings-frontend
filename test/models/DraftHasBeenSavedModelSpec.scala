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

import java.time.LocalDate

import base.SpecBase

class DraftHasBeenSavedModelSpec extends SpecBase {

  val sut: DraftHasBeenSavedModel = DraftHasBeenSavedModel()

  "GetDate returns a formatted date for 28 days from" - {
    "1st of feb" in {
      val date           = LocalDate.of(2002, 2, 1)
      val expectedResult = "01 March 2002"
      val result         = sut.getDate(date)

      result mustBe expectedResult
    }
    "1st of Feb on a leap year" in {
      val date           = LocalDate.of(2000, 2, 1)
      val expectedResult = "29 February 2000"
      val result         = sut.getDate(date)

      result mustBe expectedResult
    }
  }
}
