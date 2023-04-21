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

package pages

import scala.util.Success

import models.{DraftId, UserAnswers}
import pages.behaviours.PageBehaviours

class HaveYouUsedMethodOneForSimilarGoodsInPastPageSpec extends PageBehaviours {

  "HaveYouUsedMethodOneForSimilarGoodsInPastPage" - {

    beRetrievable[Boolean](HaveYouUsedMethodOneForSimilarGoodsInPastPage)

    beSettable[Boolean](HaveYouUsedMethodOneForSimilarGoodsInPastPage)

    beRemovable[Boolean](HaveYouUsedMethodOneForSimilarGoodsInPastPage)
  }
  "cleanup" - {
    "should reset DescribeTheSimilarGoodsPage" - {
      "when HaveYouUsedMethodOneForSimilarGoodsInPastPage is changed to No" in {
        val emptyUserAnswers = UserAnswers("id", DraftId(1))

        val ua = emptyUserAnswers
          .set(DescribeTheSimilarGoodsPage, "similarities")
          .get

        HaveYouUsedMethodOneForSimilarGoodsInPastPage.cleanup(Some(false), ua) mustBe
          Success(emptyUserAnswers)
      }
    }
  }
}
