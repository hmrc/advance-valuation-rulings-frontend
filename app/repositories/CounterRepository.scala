/*
 * Copyright 2025 HM Revenue & Customs
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

import com.google.inject.ImplementedBy
import models.{CounterId, CounterWrapper}
import org.mongodb.scala.MongoBulkWriteException
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import org.mongodb.scala.SingleObservableFuture
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@ImplementedBy(classOf[CounterMongoRepository])
trait CounterRepository {
  def ensureDraftIdIsCorrect(): Future[Unit]

  def seed: Future[Unit]

  def nextId(id: CounterId): Future[Long]
}

@Singleton
class CounterMongoRepository @Inject() (
  mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[CounterWrapper](
      collectionName = "counters",
      mongoComponent = mongoComponent,
      domainFormat = CounterWrapper.format,
      indexes = Nil
    )
    with CounterRepository {

  private val duplicateErrorCode: Int   = 11000
  private def byId(id: CounterId): Bson = Filters.eq("_id", id.toString)

  override lazy val requiresTtlIndex: Boolean = false

  val startingIndex = 7081634L

  private[repositories] val seeds: Seq[CounterWrapper] = Seq(
    CounterWrapper(CounterId.DraftId, startingIndex)
  )

  override def ensureDraftIdIsCorrect(): Future[Unit] =
    collection
      .find(byId(CounterId.DraftId))
      .headOption()
      .flatMap(_.map { draftId =>
        if (draftId.index < startingIndex) {
          collection
            .findOneAndUpdate(
              filter = byId(CounterId.DraftId),
              update = Updates.set("index", startingIndex),
              options = FindOneAndUpdateOptions()
                .upsert(true)
                .bypassDocumentValidation(false)
                .returnDocument(ReturnDocument.AFTER)
            )
            .toFuture()
            .map(_ => ())
        } else {
          Future.successful(())
        }
      }.getOrElse(Future.successful(())))

  @nowarn
  private val seedDatabase =
    seed // Eagerly call seed to ensure records are created on startup if needed

  override def seed: Future[Unit] =
    collection
      .insertMany(seeds)
      .toFuture()
      .map(_ => ())
      .recoverWith {
        case e: MongoBulkWriteException if e.getWriteErrors.asScala.forall(x => x.getCode == duplicateErrorCode) =>
          ensureDraftIdIsCorrect()
      }

  override def nextId(id: CounterId): Future[Long] =
    collection
      .findOneAndUpdate(
        filter = byId(id),
        update = Updates.inc("index", 1),
        options = FindOneAndUpdateOptions()
          .upsert(true)
          .bypassDocumentValidation(false)
          .returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
      .map(_.index)
}
