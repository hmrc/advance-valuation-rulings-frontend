package repositories

import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import com.codahale.metrics.SharedMetricRegistries
import models.{CounterId, CounterWrapper}
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

  ".nextId" - {

    "must return sequential ids" in {

      val startingValue = repository.seeds.find(_._id == CounterId.DraftId).head.index

      repository.nextId(CounterId.DraftId).futureValue mustEqual startingValue + 1
      repository.nextId(CounterId.DraftId).futureValue mustEqual startingValue + 2
      repository.nextId(CounterId.DraftId).futureValue mustEqual startingValue + 3
    }
  }
}
