package repositories

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import config.FrontendAppConfig
import models.{DraftId, UserAnswers}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val draftId = DraftId(0)

  private val userAnswers =
    UserAnswers(
      "id",
      draftId,
      Json.obj("foo" -> "bar"),
      Instant.ofEpochSecond(1)
    )

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = instant)

      val setResult     = repository.set(userAnswers).futureValue
      val updatedRecord = find(
        Filters.and(
          Filters.equal("userId", userAnswers.userId),
          Filters.equal("draftId", userAnswers.draftId)
        )
      ).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this user id and draft id" - {

      "must update the lastUpdated time and get the record" in {

        insert(userAnswers).futureValue

        val result         = repository.get(userAnswers.userId, userAnswers.draftId).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is a record for this user id with a different draft id" - {

      "must return None" in {

        val differentAnswers = userAnswers.copy(draftId = DraftId(2))

        insert(differentAnswers).futureValue

        repository.get("userId", DraftId(1)).futureValue must not be defined
      }
    }

    "when there is a record for this draft id for a different user id" - {

      "must return None" in {

        val differentAnswers = userAnswers.copy(userId = "another user id")

        insert(differentAnswers).futureValue

        repository.get("userId", DraftId(1)).futureValue must not be defined
      }
    }

    "when there is no record for this user id and draft id" - {

      "must return None" in {

        repository.get("user id that does not exist", DraftId(2)).futureValue must not be defined
      }
    }
  }

  ".clear" - {

    "must remove a record" in {

      insert(userAnswers).futureValue

      val result = repository.clear(userAnswers.userId, userAnswers.draftId).futureValue

      result mustEqual true
      repository.get(userAnswers.userId, userAnswers.draftId).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist", draftId).futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(userAnswers).futureValue

        val result = repository.keepAlive(userAnswers.userId, userAnswers.draftId).futureValue

        val expectedUpdatedAnswers = userAnswers copy (lastUpdated = instant)

        result mustEqual true

        val updatedAnswers = find(
          Filters.and(
            Filters.equal("userId", userAnswers.userId),
            Filters.equal("draftId", userAnswers.draftId)
          )
        ).futureValue.headOption.value

        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist", draftId).futureValue mustEqual true
      }
    }
  }
}
