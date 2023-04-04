package repositories

import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import com.codahale.metrics.SharedMetricRegistries
import models.DraftId
import utils.BaseIntegrationSpec

class DraftIdRepositorySpec
    extends BaseIntegrationSpec
    with DefaultPlayMongoRepositorySupport[DraftId] {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
    deleteAll().futureValue
    prepareDatabase()
  }

  override protected def checkTtlIndex = false
  override protected val repository    = new DraftIdRepositoryImpl(mongoComponent)

  private val draftIdPrefix = "GBAVR"

  "generate" - {
    "should generate an incremental draftId from a zero state" in {
      val draftId = repository.generate(draftIdPrefix).futureValue

      draftId mustBe DraftId(draftIdPrefix, 1L)
      draftId.render mustBe draftIdPrefix + "000000001"
    }

    "should generate an incremental draftId from a non-zero state" in {
      val InitialState = 3L
      repository.collection
        .insertOne(DraftId(draftIdPrefix, InitialState))
        .toFuture()
        .futureValue

      val draftId = repository.generate(draftIdPrefix).futureValue

      draftId mustBe DraftId(draftIdPrefix, InitialState + 1)
      draftId.render mustBe draftIdPrefix + s"00000000${InitialState + 1}"
    }

    "should generate a new incremental draftId when given a new prefix" in {
      val OldPrefixState = 3L
      val OldPrefix      = "OLD-PREFIX"
      val NewPrefix      = "NEW-PREFIX"
      repository.collection
        .insertOne(DraftId(OldPrefix, OldPrefixState))
        .toFuture()
        .futureValue

      val draftId = repository.generate(NewPrefix).futureValue

      draftId mustBe DraftId(NewPrefix, 1L)
      draftId.render mustBe NewPrefix + "000000001"
    }
  }
}
