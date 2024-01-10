/*
 * Copyright 2024 HM Revenue & Customs
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

package pages

import models.{DraftId, UserAnswers}
import pages.behaviours.PageBehaviours

import scala.util.Success

class IsSaleBetweenRelatedPartiesPageSpec extends PageBehaviours {

  "IsSaleBetweenRelatedPartiesPage" - {

    beRetrievable[Boolean](IsSaleBetweenRelatedPartiesPage)

    beSettable[Boolean](IsSaleBetweenRelatedPartiesPage)

    beRemovable[Boolean](IsSaleBetweenRelatedPartiesPage)
  }
  "cleanup" - {
    "should reset ExplainHowPartiesAreRelatedPage" - {

      "when IsSaleBetweenRelatedPartiesPage is changed to No" in {

        val emptyUserAnswers = UserAnswers("id", DraftId(1))

        val ua = emptyUserAnswers
          .set(ExplainHowPartiesAreRelatedPage, "reasons")
          .get

        IsSaleBetweenRelatedPartiesPage.cleanup(Some(false), ua) mustBe
          Success(emptyUserAnswers)
      }
    }
    "should do nothing" - {

      "when IsSaleBetweenRelatedPartiesPage unchanged (as Yes)" in {

        val emptyUserAnswers = UserAnswers("id", DraftId(1))

        val ua = emptyUserAnswers
          .set(ExplainHowPartiesAreRelatedPage, "reasons")
          .get

        IsSaleBetweenRelatedPartiesPage.cleanup(Some(true), ua) mustBe
          Success(ua)
      }

      "when IsSaleBetweenRelatedPartiesPage is None" in {

        val emptyUserAnswers = UserAnswers("id", DraftId(1))

        val ua = emptyUserAnswers
          .set(ExplainHowPartiesAreRelatedPage, "reasons")
          .get

        IsSaleBetweenRelatedPartiesPage.cleanup(None, ua) mustBe
          Success(ua)
      }
    }
  }
}
