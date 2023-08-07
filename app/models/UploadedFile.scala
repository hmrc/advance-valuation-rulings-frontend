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

import java.time.Instant

import play.api.libs.json._

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import enumeratum.EnumEntry.Uppercase

sealed abstract class UploadedFile extends Product with Serializable {

  def reference: String
  def fileName: Option[String]
  def fileUrl: Option[String]
  def isSuccessful: Boolean
}

object UploadedFile {

  final case class Initiated(
    reference: String
  ) extends UploadedFile {
    override val fileName: Option[String] = None
    override val fileUrl: Option[String]  = None
    override val isSuccessful: Boolean    = false
  }

  object Initiated {

    implicit lazy val format: OFormat[Initiated] = Json.format
  }

  final case class Success(
    reference: String,
    downloadUrl: String,
    uploadDetails: UploadDetails
  ) extends UploadedFile {
    override val fileName: Option[String] =
      Some(uploadDetails.fileName)
    override val fileUrl: Option[String]  =
      Some(downloadUrl)
    override val isSuccessful: Boolean    = true
  }

  object Success {

    implicit lazy val format: OFormat[Success] = Json.format
  }

  final case class Failure(
    reference: String,
    failureDetails: FailureDetails
  ) extends UploadedFile {
    override val fileName: Option[String] = None
    override val fileUrl: Option[String]  = None
    override val isSuccessful: Boolean    = false
  }

  object Failure {

    implicit lazy val format: OFormat[Failure] = Json.format
  }

  final case class UploadDetails(
    fileName: String,
    fileMimeType: String,
    uploadTimestamp: Instant,
    checksum: String,
    size: Long
  )

  object UploadDetails {

    implicit lazy val format: OFormat[UploadDetails] = Json.format
  }

  final case class FailureDetails(
    failureReason: FailureReason,
    failureMessage: Option[String]
  )

  object FailureDetails {

    implicit lazy val format: OFormat[FailureDetails] = Json.format
  }

  sealed abstract class FailureReason extends EnumEntry with Uppercase

  object FailureReason extends Enum[FailureReason] with PlayJsonEnum[FailureReason] {

    override lazy val values: IndexedSeq[FailureReason] = findValues

    case object Duplicate extends FailureReason
    case object Quarantine extends FailureReason
    case object Rejected extends FailureReason
    case object Unknown extends FailureReason
    case object InvalidArgument extends FailureReason
    case object EntityTooLarge extends FailureReason
    case object EntityTooSmall extends FailureReason
  }

  implicit lazy val reads: Reads[UploadedFile] =
    (__ \ "fileStatus").read[String].flatMap {
      case "INITIATED" => __.read[Initiated].widen
      case "READY"     => __.read[Success].widen
      case "FAILED"    => __.read[Failure].widen
      case _           => Reads.failed("error.invalid")
    }

  implicit lazy val writes: OWrites[UploadedFile] =
    OWrites {
      case i: UploadedFile.Initiated =>
        Json.toJsObject(i) ++ Json.obj("fileStatus" -> "INITIATED")
      case s: UploadedFile.Success   =>
        Json.toJsObject(s) ++ Json.obj("fileStatus" -> "READY")
      case f: UploadedFile.Failure   =>
        Json.toJsObject(f) ++ Json.obj("fileStatus" -> "FAILED")
    }
}
