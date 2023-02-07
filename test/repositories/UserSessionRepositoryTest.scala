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

import connectors.Reference
import models.fileupload.{Failed, InProgress, UploadDetails, UploadId, UploadedSuccessfully}
import org.bson.types.ObjectId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UserSessionRepositoryTest extends AnyWordSpec with Matchers {

  "Serialization and deserialization of UploadDetails" should {

    "serialize and deserialize InProgress status" in {
      val input = UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), InProgress)

      val serialized = FileUploadRepository.mongoFormat.writes(input)
      val output = FileUploadRepository.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize Failed status" in {
      val input = UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), Failed)

      val serialized = FileUploadRepository.mongoFormat.writes(input)
      val output = FileUploadRepository.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize UploadedSuccessfully status when size is unknown" in {
      val input = UploadDetails(
        ObjectId.get(),
        UploadId.generate,
        Reference("ABC"),
        UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080", size = None)
      )

      val serialized = FileUploadRepository.mongoFormat.writes(input)
      val output = FileUploadRepository.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize UploadedSuccessfully status when size is known" in {
      val input = UploadDetails(
        ObjectId.get(),
        UploadId.generate,
        Reference("ABC"),
        UploadedSuccessfully("foo.txt", "text/plain", "http:localhost:8080", size = Some(123456))
      )

      val serialized = FileUploadRepository.mongoFormat.writes(input)
      val output = FileUploadRepository.mongoFormat.reads(serialized)

      output.get shouldBe input
    }
  }
}
