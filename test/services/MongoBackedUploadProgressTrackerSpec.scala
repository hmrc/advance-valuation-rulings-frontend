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

package services

import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import models.fileupload._
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repositories.FileUploadRepository
import services.fileupload.MongoBackedUploadProgressTracker

class MongoBackedUploadProgressTrackerSpec
    extends AnyWordSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UploadDetails]
    with IntegrationPatience {

  private lazy val app = GuiceApplicationBuilder()
    .overrides(
      bind[MongoComponent].toInstance(mongoComponent)
    )
    .build()

  override lazy val repository: FileUploadRepository =
    app.injector.instanceOf[FileUploadRepository]

  private lazy val tracker = app.injector.instanceOf[MongoBackedUploadProgressTracker]

  "MongoBackedUploadProgressTracker" should {
    "coordinate workflow" in {
      val reference      = Reference("reference")
      val id             = UploadId("upload-id")
      val expectedStatus =
        UploadedSuccessfully("name", "mimeType", "downloadUrl", "checksum", size = Some(123))

      tracker.requestUpload(id, reference).futureValue
      tracker.registerUploadResult(reference, expectedStatus).futureValue

      val result = tracker.getUploadResult(id).futureValue

      result shouldBe Some(expectedStatus)
    }
  }
}
