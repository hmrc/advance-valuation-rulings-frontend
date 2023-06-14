package repositories

import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import com.codahale.metrics.SharedMetricRegistries
import models.{CounterId, CounterWrapper}
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import utils.BaseIntegrationSpec

class CounterRepositorySpec
    extends BaseIntegrationSpec
    with DefaultPlayMongoRepositorySupport[CounterWrapper] {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
    repository.seed.futureValue
  }

  override protected def checkTtlIndex = false
  override protected val repository    = new CounterRepository(mongoComponent)

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
      ).futureValue.head.index mustEqual repository.startingIndex
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
      ).futureValue.head.index mustEqual repository.startingIndex + 1
    }
  }

  ".nextId" - {

    "must return sequential ids" in {

      val startingValue = repository.seeds.find(_._id == CounterId.DraftId).head.index

      repository.nextId(CounterId.DraftId).futureValue mustEqual startingValue + 1
      repository.nextId(CounterId.DraftId).futureValue mustEqual startingValue + 2
      repository.nextId(CounterId.DraftId).futureValue mustEqual startingValue + 3
    }
  }
}
