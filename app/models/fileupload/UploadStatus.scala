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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats

import models.fileupload._
import org.bson.types.ObjectId

sealed trait UploadStatus

case object NotStarted extends UploadStatus
case object InProgress extends UploadStatus
case object Failed extends UploadStatus
case object Rejected extends UploadStatus
case object Quarantine extends UploadStatus

case class UploadedSuccessfully(
  name: String,
  mimeType: String,
  downloadUrl: String,
  size: Option[Long]
) extends UploadStatus

case class UploadDetails(
  id: ObjectId,
  uploadId: UploadId,
  reference: Reference,
  status: UploadStatus
)

object UploadStatus {
  def message(us: UploadStatus): String  =
    us match {
      case InProgress | NotStarted | _: UploadedSuccessfully => ""
      case Failed                                            => "uploadSupportingDocuments.failed"
      case Rejected                                          => "uploadSupportingDocuments.rejected"
      case Quarantine                                        => "uploadSupportingDocuments.quarantine"
    }
  def isError(us: UploadStatus): Boolean =
    us match {
      case InProgress | NotStarted | _: UploadedSuccessfully => false
      case Failed | Rejected | Quarantine                    => true
    }

  def toFormErrors(us: UploadStatus): Map[String, String] =
    us match {
      case InProgress | NotStarted | _: UploadedSuccessfully => Map.empty
      case Failed                                            => Map("file-input" -> message(Failed))
      case Rejected                                          => Map("file-input" -> message(Rejected))
      case Quarantine                                        => Map("file-input" -> message(Quarantine))
    }

  implicit val uploadStatusFormat: Format[UploadStatus] = {
    implicit val uploadedSuccessfullyFormat: OFormat[UploadedSuccessfully] =
      Json.format[UploadedSuccessfully]
    val read: Reads[UploadStatus]                                          = new Reads[UploadStatus] {
      override def reads(json: JsValue): JsResult[UploadStatus] = {
        val jsObject = json.asInstanceOf[JsObject]
        jsObject.value.get("_type") match {
          case Some(JsString("NotStarted"))           => JsSuccess(NotStarted)
          case Some(JsString("InProgress"))           => JsSuccess(InProgress)
          case Some(JsString("Failed"))               => JsSuccess(Failed)
          case Some(JsString("Quarantine"))           => JsSuccess(Quarantine)
          case Some(JsString("Rejected"))             => JsSuccess(Rejected)
          case Some(JsString("UploadedSuccessfully")) =>
            Json.fromJson[UploadedSuccessfully](jsObject)(uploadedSuccessfullyFormat)
          case Some(value)                            => JsError(s"Unexpected value of _type: $value")
          case None                                   => JsError("Missing _type field")
        }
      }
    }

    val write: Writes[UploadStatus] = new Writes[UploadStatus] {
      override def writes(p: UploadStatus): JsValue =
        p match {
          case NotStarted              => JsObject(Map("_type" -> JsString("NotStarted")))
          case InProgress              => JsObject(Map("_type" -> JsString("InProgress")))
          case Failed                  => JsObject(Map("_type" -> JsString("Failed")))
          case Quarantine              => JsObject(Map("_type" -> JsString("Quarantine")))
          case Rejected                => JsObject(Map("_type" -> JsString("Rejected")))
          case s: UploadedSuccessfully =>
            Json.toJson(s)(uploadedSuccessfullyFormat).as[JsObject] + ("_type" -> JsString(
              "UploadedSuccessfully"
            ))
        }
    }

    Format(read, write)
  }
}

object UploadDetails {
  implicit val mongoFormat: OFormat[UploadDetails] = {
    implicit val objectIdFormats: Format[ObjectId] = MongoFormats.objectIdFormat
    ((__ \ "_id").format[ObjectId]
      ~ (__ \ "uploadId").format[UploadId]
      ~ (__ \ "reference").format[Reference]
      ~ (__ \ "status")
        .format[UploadStatus])(UploadDetails.apply _, unlift(UploadDetails.unapply _))
  }

}
