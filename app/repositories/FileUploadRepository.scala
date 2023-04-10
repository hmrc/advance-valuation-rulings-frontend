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

import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

import play.api.Configuration
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import models.Done
import models.fileupload._
import org.mongodb.scala.model.{Indexes, IndexModel, IndexOptions, Updates}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import repositories.FileUploadRepository.NothingToUpdateException

@Singleton
class FileUploadRepository @Inject() (
  mongoComponent: MongoComponent,
  configuration: Configuration,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UploadDetails](
      collectionName = "uploaded-files",
      mongoComponent = mongoComponent,
      domainFormat = UploadDetails.mongoFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("uploadId"), IndexOptions().unique(true)),
        IndexModel(Indexes.ascending("reference"), IndexOptions().unique(true)),
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(configuration.get[Long]("mongodb.fileUploadTtlInDays"), TimeUnit.DAYS)
        )
      ),
      extraCodecs = Seq(
        Codecs.playFormatCodec(UploadId.idFormat),
        Codecs.playFormatCodec(Reference.referenceFormat)
      ) ++ Codecs.playFormatSumCodecs(UploadStatus.uploadStatusFormat),
      replaceIndexes = true
    ) {

  def insert(details: UploadDetails): Future[Done] =
    collection
      .insertOne(details)
      .toFuture()
      .map(_ => Done)

  def findByUploadId(uploadId: UploadId): Future[Option[UploadDetails]] =
    collection.find(equal("uploadId", uploadId)).headOption()

  def updateStatus(reference: Reference, newStatus: UploadStatus): Future[Done] =
    collection
      .findOneAndUpdate(
        filter = equal("reference", reference),
        update = Updates.combine(
          set("status", newStatus),
          set("lastUpdated", clock.instant())
        )
      )
      .headOption()
      .flatMap {
        _.map(_ => Future.successful(Done))
          .getOrElse(Future.failed(NothingToUpdateException))
      }
}

object FileUploadRepository {

  case object NothingToUpdateException extends Exception with NoStackTrace
}
