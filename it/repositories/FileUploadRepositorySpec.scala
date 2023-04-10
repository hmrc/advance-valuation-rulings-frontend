package repositories

import java.time.{Clock, Instant, ZoneOffset}
import java.time.temporal.ChronoUnit

import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import models.fileupload._
import org.bson.types.ObjectId
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class FileUploadRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UploadDetails]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues {

  override lazy val repository: FileUploadRepository =
    app.injector.instanceOf[FileUploadRepository]

  private val now         = Instant.now().truncatedTo(ChronoUnit.MILLIS)
  private val updatedTime = now.plusSeconds(60)

  private lazy val app: Application = GuiceApplicationBuilder()
    .overrides(
      bind[MongoComponent].toInstance(mongoComponent),
      bind[Clock].toInstance(Clock.fixed(updatedTime, ZoneOffset.UTC))
    )
    .build()

  private val instance = UploadDetails(
    id = ObjectId.get(),
    uploadId = UploadId("uploadId"),
    reference = Reference("reference"),
    status = NotStarted,
    lastUpdated = now
  )

  "insert" - {

    "must insert an `UploadDetails` instance" in {

      find(Filters.equal("_id", instance.id)).futureValue mustBe empty

      repository.insert(instance).futureValue

      find(Filters.equal("_id", instance.id)).futureValue.headOption.value mustEqual instance
    }

    "must fail to insert instances with duplicate `uploadId`s" in {

      val secondInstance = instance.copy(reference = Reference("different reference"))
      repository.insert(instance).futureValue
      repository.insert(secondInstance).failed.futureValue
    }

    "must fail to insert instances with duplicate `reference`s" in {

      val secondInstance = instance.copy(uploadId = UploadId("different uploadId"))
      repository.insert(instance).futureValue
      repository.insert(secondInstance).failed.futureValue
    }
  }

  "findByUploadId" - {

    "must return an instance when it has a matching upload id" in {

      insert(instance).futureValue
      repository.findByUploadId(UploadId("uploadId2")).futureValue mustBe empty
    }

    "must return `None` when there is no instance with the given upload id" in {

      insert(instance).futureValue
      repository.findByUploadId(UploadId("uploadId2")).futureValue mustBe empty
    }
  }

  "updateStatus" - {

    "must update the status of an instance when its reference matches the query" in {

      insert(instance).futureValue
      repository.updateStatus(instance.reference, InProgress).futureValue

      val result = find(
        Filters.equal("_id", instance.id)
      ).futureValue.headOption.value

      result.status mustEqual InProgress
      result.lastUpdated mustEqual updatedTime
    }

    "must fail if there is no instance which matches the query" in {

      repository.updateStatus(instance.reference, InProgress).failed.futureValue
    }
  }
}
