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

package repositories

import com.codahale.metrics.SharedMetricRegistries
import models.{CounterId, CounterWrapper}
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import utils.BaseIntegrationSpec
import org.mongodb.scala.SingleObservableFuture

class CounterRepositorySpec extends BaseIntegrationSpec with DefaultPlayMongoRepositorySupport[CounterWrapper] {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
    repository.seed.futureValue
  }

  override protected def checkTtlIndex: Boolean             = false
  override protected val repository: CounterMongoRepository = new CounterMongoRepository(mongoComponent)

  "on startup" - {

    "must insert a seed record when it does not already exist" in {

      findAll().futureValue must contain only repository.seeds.head
    }
  }

  ".seed" - {

    "must not fail when records already exist" in {

      repository.seed.futureValue

      findAll().futureValue must contain only repository.seeds.head
    }
  }

  ".ensureDraftIdIsCorrect" - {

    "must update the draft Id index when it is lower than the intended starting index" in {

      repository.seed.futureValue

      repository.collection
        .findOneAndUpdate(
          filter = Filters.eq("_id", CounterId.DraftId.toString),
          update = Updates.set("index", 1L),
          options = FindOneAndUpdateOptions()
            .upsert(true)
            .bypassDocumentValidation(false)
        )
        .toFuture()
        .futureValue

      repository.ensureDraftIdIsCorrect().futureValue

      find(
        Filters.eq("_id", CounterId.DraftId.toString)
      ).futureValue.head.index mustBe repository.startingIndex
    }

    "must not update the draft Id index when it is equal to or greater than the intended starting index" in {

      repository.seed.futureValue

      repository.collection
        .findOneAndUpdate(
          filter = Filters.eq("_id", CounterId.DraftId.toString),
          update = Updates.set("index", repository.startingIndex + 1),
          options = FindOneAndUpdateOptions()
            .upsert(true)
            .bypassDocumentValidation(false)
        )
        .toFuture()
        .futureValue

      repository.ensureDraftIdIsCorrect().futureValue

      find(
        Filters.eq("_id", CounterId.DraftId.toString)
      ).futureValue.head.index mustBe repository.startingIndex + 1
    }

    "must return Unit when no document with draft ID exists in the collection" in {

      repository.seed.futureValue

      repository.collection
        .deleteOne(Filters.eq("_id", CounterId.DraftId.toString))
        .toFuture()
        .futureValue

      repository.ensureDraftIdIsCorrect().futureValue mustBe (): Unit

      find(
        Filters.eq("_id", CounterId.DraftId.toString)
      ).futureValue mustBe empty
    }
  }

  ".nextId" - {

    "must return sequential ids" in {

      val startingValue = repository.seeds.find(_._id == CounterId.DraftId).head.index

      repository.nextId(CounterId.DraftId).futureValue mustBe startingValue + 1
      repository.nextId(CounterId.DraftId).futureValue mustBe startingValue + 2
      repository.nextId(CounterId.DraftId).futureValue mustBe startingValue + 3
    }
  }
}
