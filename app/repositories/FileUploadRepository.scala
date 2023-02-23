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
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import models.fileupload._
import org.mongodb.scala.model.{FindOneAndUpdateOptions, Indexes, IndexModel, IndexOptions}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set

@Singleton
class FileUploadRepository @Inject() (mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[UploadDetails](
      collectionName = "uploaded-files",
      mongoComponent = mongoComponent,
      domainFormat = UploadDetails.mongoFormat,
      indexes = Seq(
        IndexModel(Indexes.ascending("uploadId"), IndexOptions().unique(true)),
        IndexModel(Indexes.ascending("reference"), IndexOptions().unique(true))
      ),
      replaceIndexes = true
    ) {

  def insert(details: UploadDetails): Future[Unit] =
    collection
      .insertOne(details)
      .toFuture()
      .map(_ => ())

  def findByUploadId(uploadId: UploadId): Future[Option[UploadDetails]] =
    collection.find(equal("uploadId", Codecs.toBson(uploadId))).headOption()

  def updateStatus(reference: Reference, newStatus: UploadStatus): Future[UploadStatus] =
    collection
      .findOneAndUpdate(
        filter = equal("reference", Codecs.toBson(reference)),
        update = set("status", Codecs.toBson(newStatus)),
        options = FindOneAndUpdateOptions().upsert(true)
      )
      .toFuture()
      .map(_.status)
}
