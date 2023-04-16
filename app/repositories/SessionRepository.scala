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

package repositories

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import config.FrontendAppConfig
import models.{DraftId, UserAnswers}
import org.bson.conversions.Bson
import org.mongodb.scala.model._

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UserAnswers](
      collectionName = "user-answers",
      mongoComponent = mongoComponent,
      domainFormat = UserAnswers.mongoFormat,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        ),
        IndexModel(
          Indexes.ascending("userId", "draftId"),
          IndexOptions()
            .name("id-index")
            .unique(true)
        )
      ),
      extraCodecs = Seq(Codecs.playFormatCodec(DraftId.format))
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byUserIdAndDraftId(userId: String, draftId: DraftId): Bson =
    Filters.and(
      Filters.eq("userId", userId),
      Filters.eq("draftId", draftId)
    )

  def keepAlive(userId: String, draftId: DraftId): Future[Boolean] =
    collection
      .updateOne(
        filter = byUserIdAndDraftId(userId, draftId),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(_ => true)

  def get(userId: String, draftId: DraftId): Future[Option[UserAnswers]] =
    keepAlive(userId, draftId).flatMap {
      _ =>
        collection
          .find(byUserIdAndDraftId(userId, draftId))
          .headOption()
    }

  def set(answers: UserAnswers): Future[Boolean] = {

    val updatedAnswers = answers copy (lastUpdated = Instant.now(clock))

    collection
      .replaceOne(
        filter = byUserIdAndDraftId(updatedAnswers.userId, updatedAnswers.draftId),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(userId: String, draftId: DraftId): Future[Boolean] =
    collection
      .deleteOne(byUserIdAndDraftId(userId, draftId))
      .toFuture()
      .map(_ => true)
}
