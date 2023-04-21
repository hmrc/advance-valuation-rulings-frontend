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

package models

import java.time.{LocalDateTime, ZoneOffset}

import play.api.libs.json.Json

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UploadedFileSpec extends AnyFreeSpec with Matchers {

  private val instant = LocalDateTime
    .of(2023, 3, 2, 12, 30, 45)
    .toInstant(ZoneOffset.UTC)

  "Initiate" - {

    val model: UploadedFile = UploadedFile.Initiated("reference")

    val json = Json.obj(
      "reference"  -> "reference",
      "fileStatus" -> "INITIATED"
    )

    "must read from json" in {
      json.as[UploadedFile] mustEqual model
    }

    "must write to json" in {
      Json.toJson(model) mustEqual json
    }
  }

  "Success" - {

    val model: UploadedFile = UploadedFile.Success(
      reference = "reference",
      downloadUrl = "downloadUrl",
      uploadDetails = UploadedFile.UploadDetails(
        fileName = "fileName",
        fileMimeType = "fileMimeType",
        uploadTimestamp = instant,
        checksum = "checksum"
      )
    )

    val json = Json.obj(
      "reference"     -> "reference",
      "fileStatus"    -> "READY",
      "downloadUrl"   -> "downloadUrl",
      "uploadDetails" -> Json.obj(
        "fileName"        -> "fileName",
        "fileMimeType"    -> "fileMimeType",
        "uploadTimestamp" -> "2023-03-02T12:30:45Z",
        "checksum"        -> "checksum"
      )
    )

    "must read from json" in {
      json.as[UploadedFile] mustEqual model
    }

    "must write to json" in {
      Json.toJson(model) mustEqual json
    }
  }

  "Failure" - {

    val model: UploadedFile = UploadedFile.Failure(
      reference = "reference",
      failureDetails = UploadedFile.FailureDetails(
        failureReason = UploadedFile.FailureReason.Quarantine,
        failureMessage = Some("failureMessage")
      )
    )

    val json = Json.obj(
      "reference"      -> "reference",
      "fileStatus"     -> "FAILED",
      "failureDetails" -> Json.obj(
        "failureReason"  -> "QUARANTINE",
        "failureMessage" -> "failureMessage"
      )
    )

    "must read from json" in {
      json.as[UploadedFile] mustEqual model
    }

    "must write to json" in {
      Json.toJson(model) mustEqual json
    }
  }

  "must fail to read an invalid fileStatus" in {

    val json = Json.obj(
      "reference"  -> "reference",
      "fileStatus" -> "INVALID"
    )

    val result = json.validate[UploadedFile]

    result.isError mustBe true
  }
}
