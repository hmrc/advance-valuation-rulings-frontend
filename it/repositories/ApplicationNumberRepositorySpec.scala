package repositories

import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import com.codahale.metrics.SharedMetricRegistries
import models.ApplicationNumber
import utils.BaseIntegrationSpec

class ApplicationNumberRepositorySpec
    extends BaseIntegrationSpec
    with DefaultPlayMongoRepositorySupport[ApplicationNumber] {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
    deleteAll().futureValue
    prepareDatabase()
  }

  override protected def checkTtlIndex = false
  override protected val repository    = new ApplicationNumberRepositoryImpl(mongoComponent)

  private val ApplicationNumberPrefix = "GBAVR"

  "generate" - {
    "should generate an incremental applicationNumber from a zero state" in {
      val applicationNumber = repository.generate(ApplicationNumberPrefix).futureValue

      applicationNumber mustBe ApplicationNumber(ApplicationNumberPrefix, 1L)
      applicationNumber.render mustBe ApplicationNumberPrefix + "000000001"
    }

    "should generate an incremental applicationNumber from a non-zero state" in {
      val InitialState = 3L
      repository.collection
        .insertOne(ApplicationNumber(ApplicationNumberPrefix, InitialState))
        .toFuture()
        .futureValue

      val applicationNumber = repository.generate(ApplicationNumberPrefix).futureValue

      applicationNumber mustBe ApplicationNumber(ApplicationNumberPrefix, InitialState + 1)
      applicationNumber.render mustBe ApplicationNumberPrefix + s"00000000${InitialState + 1}"
    }

    "should generate a new incremental applicationNumber when given a new prefix" in {
      val OldPrefixState = 3L
      val OldPrefix      = "OLD-PREFIX"
      val NewPrefix      = "NEW-PREFIX"
      repository.collection
        .insertOne(ApplicationNumber(OldPrefix, OldPrefixState))
        .toFuture()
        .futureValue

      val applicationNumber = repository.generate(NewPrefix).futureValue

      applicationNumber mustBe ApplicationNumber(NewPrefix, 1L)
      applicationNumber.render mustBe NewPrefix + "000000001"
    }
  }
}
