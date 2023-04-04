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

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import models.DraftId
import org.mongodb.scala.model._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending

@Singleton
class DraftIdRepositoryImpl @Inject() (mongoComponent: MongoComponent)(implicit
  ec: ExecutionContext
) extends PlayMongoRepository[DraftId](
      collectionName = "draft-id-generator",
      domainFormat = DraftId.format,
      mongoComponent = mongoComponent,
      indexes = Seq(
        IndexModel(
          ascending("prefix"),
          IndexOptions().unique(true).name("prefixIdx")
        )
      )
    )
    with DraftIdRepository {

  override def generate(prefix: String): Future[DraftId] =
    collection
      .findOneAndUpdate(
        filter = equal("prefix", prefix),
        update = Updates.inc("value", 1),
        options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()

}
