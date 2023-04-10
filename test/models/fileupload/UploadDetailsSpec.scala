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

package models.fileupload

import java.time.Instant
import java.time.temporal.ChronoUnit

import org.bson.types.ObjectId
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class UploadDetailsTest extends AnyWordSpec with Matchers {

  private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

  "Serialization and deserialization of UploadDetails" should {

    "serialize and deserialize InProgress status" in {
      val input =
        UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), InProgress, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize Failed status" in {
      val input = UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), Failed, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize Rejected status" in {
      val input = UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), Rejected, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize Quarantine status" in {
      val input =
        UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), Quarantine, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize NotStarted status" in {
      val input =
        UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), NotStarted, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize NoFileProvided status" in {
      val input =
        UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), NoFileProvided, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize EntityTooLarge status" in {
      val input =
        UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), EntityTooLarge, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize EntityTooSmall status" in {
      val input =
        UploadDetails(ObjectId.get(), UploadId.generate, Reference("ABC"), EntityTooSmall, now)

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize UploadedSuccessfully status when size is unknown" in {
      val input = UploadDetails(
        ObjectId.get(),
        UploadId.generate,
        Reference("ABC"),
        UploadedSuccessfully(
          "foo.txt",
          "text/plain",
          "http:localhost:8080",
          "checksum",
          size = None
        ),
        now
      )

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }

    "serialize and deserialize UploadedSuccessfully status when size is known" in {
      val input = UploadDetails(
        ObjectId.get(),
        UploadId.generate,
        Reference("ABC"),
        UploadedSuccessfully(
          "foo.txt",
          "text/plain",
          "http:localhost:8080",
          "checksum",
          size = Some(123456)
        ),
        now
      )

      val serialized = UploadDetails.mongoFormat.writes(input)
      val output     = UploadDetails.mongoFormat.reads(serialized)

      output.get shouldBe input
    }
  }
}
